package com.frafio.myfinance.core.data.manager

import android.util.Log
import androidx.room.withTransaction
import com.frafio.myfinance.core.data.dao.BaseDao
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

abstract class BaseSyncManager<T : Transaction>(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val database: MyFinanceDatabase,
    private val clazz: Class<T>
) {
    companion object {
        const val DEFAULT_LIMIT_EXPENSES: Long = 50
        const val DEFAULT_LIMIT_INCOMES: Long = 100
        const val SYNC_THRESHOLD_MS = 29L * 24L * 60L * 60L * 1000L // 29 days in ms
    }
    protected val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    protected abstract val collectionName: String
    protected abstract val baseDao: BaseDao<T>
    protected abstract val listUpdateSuccessCode: FinanceCode
    protected abstract val listUpdateFailureCode: FinanceCode
    protected abstract val addSuccessCode: FinanceCode
    protected abstract val addFailureCode: FinanceCode
    protected abstract val editSuccessCode: FinanceCode
    protected abstract val editFailureCode: FinanceCode
    protected abstract val deleteSuccessCode: FinanceCode
    protected abstract val deleteFailureCode: FinanceCode

    protected abstract suspend fun getLastSync(userPrefs: UserPreferencesData): Long
    protected abstract suspend fun updateLastSync(timestamp: Long)
    
    protected open fun onPreUpsert(item: T, labels: List<String>): T = item

    protected suspend fun getUserEmail(): String? {
        return userPreferencesRepository.userPreferencesFlow.first().user?.email
    }

    suspend fun add(item: T): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(addFailureCode)
        val itemWithTime = copyWithSyncFields(item, updatedAt = System.currentTimeMillis(), isDeleted = false, deleteAt = null)
        
        return@withContext try {
            val documentReference = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(collectionName)
                .add(itemWithTime).await()
            
            // Set ID
            when (itemWithTime) {
                is Expense -> itemWithTime.id = documentReference.id
                is Income -> itemWithTime.id = documentReference.id
            }
            
            baseDao.upsert(itemWithTime)
            FinanceResult(addSuccessCode)
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error adding item to $collectionName: ${e.localizedMessage}")
            FinanceResult(addFailureCode)
        }
    }

    suspend fun edit(item: T): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(editFailureCode)
        val itemWithTime = copyWithSyncFields(item, updatedAt = System.currentTimeMillis(), isDeleted = false, deleteAt = null)
        
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(collectionName)
                .document(itemWithTime.id).set(itemWithTime).await()
            
            baseDao.upsert(itemWithTime)
            FinanceResult(editSuccessCode)
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error editing item in $collectionName: ${e.localizedMessage}")
            FinanceResult(editFailureCode)
        }
    }

    suspend fun delete(item: T): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(deleteFailureCode)
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val deleteAtDate = calendar.time
        
        val deletedItem = copyWithSyncFields(
            item, 
            updatedAt = System.currentTimeMillis(), 
            isDeleted = true, 
            deleteAt = deleteAtDate
        )
        
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(collectionName)
                .document(deletedItem.id).set(deletedItem).await()
            
            baseDao.deleteById(deletedItem.id)
            FinanceResult(deleteSuccessCode)
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error deleting item from $collectionName: ${e.localizedMessage}")
            FinanceResult(deleteFailureCode)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyWithSyncFields(item: T, updatedAt: Long, isDeleted: Boolean, deleteAt: java.util.Date?): T {
        return when (item) {
            is Expense -> item.copy(updatedAt = updatedAt, isDeleted = isDeleted, deleteAt = deleteAt) as T
            is Income -> item.copy(updatedAt = updatedAt, isDeleted = isDeleted, deleteAt = deleteAt) as T
            else -> item
        }
    }

    private var snapshotListener: ListenerRegistration? = null

    private suspend fun performFullSync(email: String): Long? = withContext(Dispatchers.IO) {
        try {
            val snapshots = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(collectionName)
                .get()
                .await()

            val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels
            val remoteItems = snapshots.documents.mapNotNull { doc ->
                val item = doc.toObject(clazz)
                when (item) {
                    is Expense -> item.id = doc.id
                    is Income -> item.id = doc.id
                }
                item
            }

            val remoteIds = remoteItems.map { it.id }.toSet()
            val localItems = baseDao.getAllSync()
            val localIds = localItems.map { it.id }.toSet()

            database.withTransaction {
                // Upsert remote items (if not deleted)
                remoteItems.forEach { item ->
                    if (item.isDeleted == true) {
                        baseDao.deleteById(item.id)
                    } else {
                        val finalItem = onPreUpsert(item, currentLabels)
                        baseDao.upsert(finalItem)
                    }
                }

                // Delete local items that are not in remote
                localIds.forEach { id ->
                    if (!remoteIds.contains(id)) {
                        baseDao.deleteById(id)
                    }
                }
            }

            val maxUpdatedAt = remoteItems.mapNotNull { it.updatedAt }.maxOrNull() ?: System.currentTimeMillis()
            updateLastSync(maxUpdatedAt)
            return@withContext maxUpdatedAt
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error during full sync for $collectionName: ${e.localizedMessage}")
            return@withContext null
        }
    }

    fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>? = null,
        onError: ((FirebaseFirestoreException) -> Unit)? = null
    ) {
        if (snapshotListener != null) {
            onInitialSync?.complete(Unit)
            return
        }

        scope.launch(Dispatchers.IO) {
            val userPrefs = userPreferencesRepository.userPreferencesFlow.first()
            val email = userPrefs.user?.email ?: run {
                onInitialSync?.complete(Unit)
                return@launch
            }
            var currentLastSync = getLastSync(userPrefs)
            val currentTime = System.currentTimeMillis()

            if (currentLastSync != 0L && currentTime - currentLastSync >= SYNC_THRESHOLD_MS) {
                Log.d("BaseSyncManager", "Performing full sync for $collectionName (last sync was > 29 days ago)")
                val newLastSync = performFullSync(email)
                if (newLastSync != null) {
                    currentLastSync = newLastSync
                }
            }

            var isFirstSnapshot = true

            Log.d("BaseSyncManager", "Starting listener for $collectionName. lastSync: $currentLastSync, user: $email")

            snapshotListener = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(collectionName)
                .whereGreaterThan(FirestoreEnums.FIELDS.UPDATED_AT.value, currentLastSync)
                .orderBy(FirestoreEnums.FIELDS.UPDATED_AT.value, Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("BaseSyncManager", "Listen failed for $collectionName: ${error.localizedMessage}")
                        onInitialSync?.complete(Unit)
                        onError?.invoke(error)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        scope.launch(Dispatchers.IO) {
                            if (!snapshots.isEmpty) {
                                var maxUpdatedAt = currentLastSync
                                val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels
                                try {
                                    database.withTransaction {
                                        snapshots.documentChanges.forEach { dc ->
                                            val item = dc.document.toObject(clazz)
                                            when (item) {
                                                is Expense -> item.id = dc.document.id
                                                is Income -> item.id = dc.document.id
                                            }

                                            if (item.updatedAt != null && item.updatedAt!! > maxUpdatedAt) {
                                                maxUpdatedAt = item.updatedAt!!
                                            }

                                            if (item.isDeleted == true) {
                                                baseDao.deleteById(item.id)
                                            } else {
                                                val finalItem = onPreUpsert(item, currentLabels)
                                                baseDao.upsert(finalItem)
                                            }
                                        }
                                    }
                                    if (maxUpdatedAt > currentLastSync) {
                                        currentLastSync = maxUpdatedAt
                                        updateLastSync(maxUpdatedAt)
                                    }
                                } catch (e: Exception) {
                                    Log.e("BaseSyncManager", "Critical error in snapshot processor for $collectionName", e)
                                }
                            }

                            if (isFirstSnapshot) {
                                isFirstSnapshot = false
                                onInitialSync?.complete(Unit)
                            }
                        }
                    } else if (isFirstSnapshot) {
                        isFirstSnapshot = false
                        onInitialSync?.complete(Unit)
                    }
                }
        }
    }

    open fun stopSnapshotListener() {
        snapshotListener?.remove()
        snapshotListener = null
    }
}
