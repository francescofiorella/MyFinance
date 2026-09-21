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
import com.frafio.myfinance.core.data.remote.RemoteDataSource
import com.frafio.myfinance.core.data.remote.RemoteListener
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import com.frafio.myfinance.core.utils.currentDeleteAtUTC
import com.frafio.myfinance.core.utils.currentTimestampUTC
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseSyncManager<T : Transaction>(
    private val userPreferencesRepository: UserPreferencesRepository,
    protected val database: MyFinanceDatabase,
    private val clazz: Class<T>,
    protected val remote: RemoteDataSource,
    protected val ioDispatcher: CoroutineDispatcher
) {
    companion object {
        const val DEFAULT_LIMIT_EXPENSES: Long = 50
        const val DEFAULT_LIMIT_INCOMES: Long = 100
        const val SYNC_THRESHOLD_MS = 29L * 24L * 60L * 60L * 1000L // 29 days in ms
    }
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
    protected abstract suspend fun getLastAppSync(userPrefs: UserPreferencesData): Long
    protected abstract suspend fun updateLastAppSync(timestamp: Long)
    
    protected open fun onPreUpsert(item: T, labels: List<String>): T = item

    protected suspend fun getUserEmail(): String? {
        return userPreferencesRepository.userPreferencesFlow.first().user?.email
    }

    suspend fun add(item: T): FinanceResult = withContext(ioDispatcher) {
        val email = getUserEmail() ?: return@withContext FinanceResult(addFailureCode)
        val itemWithTime = copyWithSyncFields(item, updatedAt = currentTimestampUTC(), isDeleted = false, deleteAt = null)
        
        return@withContext try {
            val remoteId = remote.add(email, collectionName, itemWithTime)
            
            // Set ID
            when (itemWithTime) {
                is Expense -> itemWithTime.id = remoteId
                is Income -> itemWithTime.id = remoteId
            }
            
            baseDao.upsert(itemWithTime)
            FinanceResult(addSuccessCode)
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error adding item to $collectionName: ${e.localizedMessage}")
            FinanceResult(addFailureCode)
        }
    }

    suspend fun edit(item: T): FinanceResult = withContext(ioDispatcher) {
        val email = getUserEmail() ?: return@withContext FinanceResult(editFailureCode)
        val itemWithTime = copyWithSyncFields(item, updatedAt = currentTimestampUTC(), isDeleted = false, deleteAt = null)
        
        return@withContext try {
            remote.set(email, collectionName, itemWithTime.id, itemWithTime)
            
            baseDao.upsert(itemWithTime)
            FinanceResult(editSuccessCode)
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error editing item in $collectionName: ${e.localizedMessage}")
            FinanceResult(editFailureCode)
        }
    }

    suspend fun delete(item: T): FinanceResult = withContext(ioDispatcher) {
        val email = getUserEmail() ?: return@withContext FinanceResult(deleteFailureCode)
        
        val deleteAtDate = currentDeleteAtUTC()
        
        val deletedItem = copyWithSyncFields(
            item, 
            updatedAt = currentTimestampUTC(), 
            isDeleted = true, 
            deleteAt = deleteAtDate
        )
        
        return@withContext try {
            remote.set(email, collectionName, deletedItem.id, deletedItem)
            
            baseDao.deleteById(deletedItem.id)
            FinanceResult(deleteSuccessCode)
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error deleting item from $collectionName: ${e.localizedMessage}")
            FinanceResult(deleteFailureCode)
        }
    }

    protected suspend fun updateArrayField(
        id: String,
        fieldName: String,
        value: Any,
        isAddition: Boolean
    ): Long? = withContext(ioDispatcher) {
        val email = getUserEmail() ?: return@withContext null
        val updatedAt = currentTimestampUTC()
        
        return@withContext try {
            remote.updateArrayField(email, collectionName, id, fieldName, value, isAddition, updatedAt)
            updatedAt
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error updating array field $fieldName in $collectionName: ${e.localizedMessage}")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun copyWithSyncFields(item: T, updatedAt: Long, isDeleted: Boolean, deleteAt: Long?): T {
        return when (item) {
            is Expense -> item.copy(updatedAt = updatedAt, isDeleted = isDeleted, deleteAt = deleteAt) as T
            is Income -> item.copy(updatedAt = updatedAt, isDeleted = isDeleted, deleteAt = deleteAt) as T
        }
    }

    private var snapshotListener: RemoteListener? = null

    private suspend fun performFullSync(email: String): Long? = withContext(ioDispatcher) {
        try {
            val documents = remote.getAll(email, collectionName, clazz)

            val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels
            val remoteItems = documents.mapNotNull { doc ->
                if (!DocumentIntegrity.verify(doc.path, doc.data)) return@mapNotNull null
                val item = doc.decode()
                when (item) {
                    is Expense -> item.id = doc.id
                    is Income -> item.id = doc.id
                }
                item
            }
            Log.i(
                DocumentIntegrity.TAG,
                "$collectionName: ${remoteItems.size} of ${documents.size} documents loaded"
            )

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

            val maxUpdatedAt = remoteItems.mapNotNull { it.updatedAt }.maxOrNull() ?: currentTimestampUTC()
            updateLastSync(maxUpdatedAt)
            updateLastAppSync(currentTimestampUTC())
            return@withContext maxUpdatedAt
        } catch (e: Exception) {
            Log.e("BaseSyncManager", "Error during full sync for $collectionName: ${e.localizedMessage}")
            return@withContext null
        }
    }

    fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>? = null
    ) {
        if (snapshotListener != null) {
            onInitialSync?.complete(Unit)
            return
        }

        scope.launch(ioDispatcher) {
            val userPrefs = userPreferencesRepository.userPreferencesFlow.first()
            val email = userPrefs.user?.email ?: run {
                onInitialSync?.complete(Unit)
                return@launch
            }
            var currentLastSync = getLastSync(userPrefs)
            val currentLastAppSync = getLastAppSync(userPrefs)
            val currentTime = currentTimestampUTC()

            if (currentLastAppSync != 0L && currentTime - currentLastAppSync >= SYNC_THRESHOLD_MS) {
                Log.d("BaseSyncManager", "Performing full sync for $collectionName (last sync was > 29 days ago)")
                val newLastSync = performFullSync(email)
                if (newLastSync != null) {
                    currentLastSync = newLastSync
                }
            }

            var isFirstSnapshot = true

            Log.d("BaseSyncManager", "Starting listener for $collectionName. lastSync: $currentLastSync, user: $email")

            snapshotListener = remote.listenChanges(email, collectionName, currentLastSync, clazz) { snapshots, error ->
                if (error != null) {
                    Log.e("BaseSyncManager", "Listen failed for $collectionName: ${error.localizedMessage}")
                    onInitialSync?.complete(Unit)
                    return@listenChanges
                }

                if (snapshots != null) {
                    scope.launch(ioDispatcher) {
                        if (!snapshots.isEmpty) {
                            updateLastAppSync(currentTimestampUTC())
                            var maxUpdatedAt = currentLastSync
                            var loaded = 0
                            val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels
                            try {
                                database.withTransaction {
                                    snapshots.changes.forEach { doc ->
                                        if (!DocumentIntegrity.verify(doc.path, doc.data)) return@forEach
                                        loaded++
                                        val item = doc.decode() ?: return@forEach
                                        when (item) {
                                            is Expense -> item.id = doc.id
                                            is Income -> item.id = doc.id
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
                                if (isFirstSnapshot) {
                                    Log.i(
                                        DocumentIntegrity.TAG,
                                        "$collectionName: $loaded of ${snapshots.changes.size} documents loaded"
                                    )
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
