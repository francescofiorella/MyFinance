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
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
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

    protected abstract suspend fun getLastSync(userPrefs: com.frafio.myfinance.core.data.repository.UserPreferencesData): Long
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

    fun startSnapshotListener(scope: CoroutineScope) {
        if (snapshotListener != null) return

        scope.launch(Dispatchers.IO) {
            val userPrefs = userPreferencesRepository.userPreferencesFlow.first()
            val email = userPrefs.user?.email ?: return@launch
            val labels = userPrefs.labels
            var currentLastSync = getLastSync(userPrefs)

            Log.d("BaseSyncManager", "Starting listener for $collectionName. lastSync: $currentLastSync, user: $email")

            snapshotListener = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(collectionName)
                .whereGreaterThan(FirestoreEnums.FIELDS.UPDATED_AT.value, currentLastSync)
                .orderBy(FirestoreEnums.FIELDS.UPDATED_AT.value, Query.Direction.ASCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("BaseSyncManager", "Listen failed for $collectionName: ${error.localizedMessage}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        if (!snapshots.isEmpty) {
                            scope.launch(Dispatchers.IO) {
                                var maxUpdatedAt = currentLastSync
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
                                                val finalItem = onPreUpsert(item, labels)
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
                        }
                    }
                }
        }
    }

    fun stopSnapshotListener() {
        snapshotListener?.remove()
        snapshotListener = null
    }
}
