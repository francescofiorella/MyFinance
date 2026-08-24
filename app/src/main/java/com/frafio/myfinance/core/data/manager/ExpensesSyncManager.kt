package com.frafio.myfinance.core.data.manager

import com.frafio.myfinance.core.data.dao.ExpenseDao
import androidx.room.withTransaction
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.storage.MyFinanceDatabase
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesSyncManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val expensesLocalRepository: ExpensesLocalRepository,
    database: MyFinanceDatabase,
    expenseDao: ExpenseDao
) : BaseSyncManager<Expense>(userPreferencesRepository, database, Expense::class.java) {

    override val collectionName: String = FirestoreEnums.FIELDS.PAYMENTS.value
    override val baseDao = expenseDao
    override val listUpdateSuccessCode = FinanceCode.EXPENSE_LIST_UPDATE_SUCCESS
    override val listUpdateFailureCode = FinanceCode.EXPENSE_LIST_UPDATE_FAILURE
    override val addSuccessCode = FinanceCode.EXPENSE_ADD_SUCCESS
    override val addFailureCode = FinanceCode.EXPENSE_ADD_FAILURE
    override val editSuccessCode = FinanceCode.EXPENSE_EDIT_SUCCESS
    override val editFailureCode = FinanceCode.EXPENSE_EDIT_FAILURE
    override val deleteSuccessCode = FinanceCode.EXPENSE_DELETE_SUCCESS
    override val deleteFailureCode = FinanceCode.EXPENSE_DELETE_FAILURE

    override suspend fun getLastSync(userPrefs: UserPreferencesData): Long = userPrefs.lastExpensesSync
    override suspend fun updateLastSync(timestamp: Long) = userPreferencesRepository.updateLastExpensesSync(timestamp)
    override suspend fun getLastAppSync(userPrefs: UserPreferencesData): Long = userPrefs.lastExpensesAppSync
    override suspend fun updateLastAppSync(timestamp: Long) = userPreferencesRepository.updateLastExpensesAppSync(timestamp)

    override fun onPreUpsert(item: Expense, labels: List<String>): Expense {
        if (labels.isEmpty() && item.labels.isNotEmpty()) return item
        val newLabels = item.labels.toMutableList()
        var changed = false
        for (label in item.labels) {
            if (!labels.contains(label)) {
                newLabels.remove(label)
                changed = true
            }
        }
        return if (changed) {
            item.copy(labels = newLabels, updatedAt = System.currentTimeMillis())
        } else item
    }

    suspend fun setMonthlyBudget(budget: Double): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .set(
                    hashMapOf(FirestoreEnums.FIELDS.MONTHLY_BUDGET.value to budget),
                    SetOptions.merge()
                ).await()
            userPreferencesRepository.updateMonthlyBudget(budget)
            FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
        } catch (_: Exception) {
            FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        }
    }

    suspend fun setCurrencyCode(currencyCode: String): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .set(
                    hashMapOf(FirestoreEnums.FIELDS.CURRENCY_CODE.value to currencyCode),
                    SetOptions.merge()
                ).await()
            userPreferencesRepository.updateCurrencyCode(currencyCode)
            FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
        } catch (_: Exception) {
            FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        }
    }

    suspend fun setProPicChoice(choice: String): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .set(
                    hashMapOf(FirestoreEnums.FIELDS.PRO_PIC_CHOICE.value to choice),
                    SetOptions.merge()
                ).await()
            userPreferencesRepository.updateProPicChoice(choice)
            FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
        } catch (_: Exception) {
            FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        }
    }

    suspend fun setLabels(
        labels: List<String>,
        successCode: FinanceCode = FinanceCode.LABELS_UPDATE_SUCCESS
    ): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        val sortedLabels = labels.sorted()
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .set(hashMapOf(FirestoreEnums.FIELDS.LABELS.value to sortedLabels), SetOptions.merge())
                .await()
            userPreferencesRepository.updateLabels(sortedLabels)
            FinanceResult(successCode)
        } catch (_: Exception) {
            FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        }
    }

    suspend fun addLabel(label: String): FinanceResult = withContext(Dispatchers.IO) {
        val trimmedLabel = label.trim()
        if (trimmedLabel.isEmpty()) return@withContext FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels
        if (currentLabels.contains(trimmedLabel)) return@withContext FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        setLabels(currentLabels + trimmedLabel, FinanceCode.LABEL_ADD_SUCCESS)
    }

    suspend fun updateExpenseLabels(
        expenseId: String,
        label: String,
        isAddition: Boolean
    ): FinanceResult = withContext(Dispatchers.IO) {
        val updatedAt = updateArrayField(
            expenseId,
            FirestoreEnums.FIELDS.LABELS.value,
            label,
            isAddition
        ) ?: return@withContext FinanceResult(editFailureCode)

        return@withContext try {
            database.withTransaction {
                val expense = baseDao.getById(expenseId)
                if (expense != null) {
                    val updatedLabels = expense.labels.toMutableList()
                    if (isAddition) {
                        if (!updatedLabels.contains(label)) updatedLabels.add(label)
                    } else {
                        updatedLabels.remove(label)
                    }
                    baseDao.upsert(expense.copy(labels = updatedLabels, updatedAt = updatedAt))
                }
            }
            FinanceResult(editSuccessCode)
        } catch (e: Exception) {
            android.util.Log.e("ExpensesSyncManager", "Error updating local labels: ${e.localizedMessage}")
            FinanceResult(editFailureCode)
        }
    }

    suspend fun deleteLabel(label: String): DeleteLabelResult = withContext(Dispatchers.IO) {
        val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels.toMutableList()
        if (!currentLabels.remove(label)) return@withContext DeleteLabelResult(FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE))

        val result = setLabels(currentLabels, FinanceCode.LABEL_DELETE_SUCCESS)
        if (result.code == FinanceCode.LABELS_UPDATE_FAILURE.code) {
            return@withContext DeleteLabelResult(result)
        }

        val allExpenses = expensesLocalRepository.getAllSync()
        val affectedExpenses = allExpenses.filter { it.labels.contains(label) }

        affectedExpenses.forEach { expense ->
            val updatedLabels = expense.labels.toMutableList()
            updatedLabels.remove(label)
            val updatedExpense = expense.copy(
                timestamp = dateToUTCTimestamp(
                    expense.year!!,
                    expense.month!!,
                    expense.day!!
                ),
                labels = updatedLabels
            )
            edit(updatedExpense)
        }

        DeleteLabelResult(result, affectedExpenses)
    }

    suspend fun undoDeleteLabel(label: String, affectedExpenses: List<Expense>): FinanceResult = withContext(Dispatchers.IO) {
        val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels.toMutableList()
        if (!currentLabels.contains(label)) {
            currentLabels.add(label)
            val result = setLabels(currentLabels, FinanceCode.LABEL_ADD_SUCCESS)
            if (result.code == FinanceCode.LABELS_UPDATE_FAILURE.code) return@withContext result
        }

        affectedExpenses.forEach { expense ->
            edit(expense)
        }
        FinanceResult(FinanceCode.LABELS_UPDATE_SUCCESS)
    }

    suspend fun editLabel(oldName: String, newName: String): FinanceResult = withContext(Dispatchers.IO) {
        val trimmedNewName = newName.trim()
        if (trimmedNewName.isEmpty()) return@withContext FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        val currentLabels = userPreferencesRepository.userPreferencesFlow.first().labels.toMutableList()
        val index = currentLabels.indexOf(oldName)
        if (index == -1) return@withContext FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)

        currentLabels[index] = trimmedNewName
        val result = setLabels(currentLabels, FinanceCode.LABEL_UPDATE_SUCCESS)
        if (result.code == FinanceCode.LABELS_UPDATE_FAILURE.code) return@withContext result

        val allExpenses = expensesLocalRepository.getAllSync()
        allExpenses.filter { it.labels.contains(oldName) }.forEach { expense ->
            val updatedLabels = expense.labels.map { if (it == oldName) trimmedNewName else it }
            val updatedExpense = expense.copy(
                timestamp = dateToUTCTimestamp(
                    expense.year!!,
                    expense.month!!,
                    expense.day!!
                ),
                labels = updatedLabels
            )
            edit(updatedExpense)
        }
        result
    }

    suspend fun setDynamicColorActive(active: Boolean) {
        userPreferencesRepository.updateDynamicColor(active)
    }

    private var rootListener: ListenerRegistration? = null

    fun startRootSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>? = null,
        onError: ((FirebaseFirestoreException) -> Unit)? = null
    ) {
        if (rootListener != null) {
            onInitialSync?.complete(Unit)
            return
        }

        scope.launch(Dispatchers.IO) {
            val email = getUserEmail() ?: run {
                onInitialSync?.complete(Unit)
                return@launch
            }
            var isFirstSnapshot = true
            rootListener = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onInitialSync?.complete(Unit)
                        onError?.invoke(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        scope.launch(Dispatchers.IO) {
                            val budget = snapshot.data?.get(FirestoreEnums.FIELDS.MONTHLY_BUDGET.value)
                                .toString().toDoubleOrNull() ?: 0.0
                            userPreferencesRepository.updateMonthlyBudget(budget)

                            val currencyCode = snapshot.data?.get(FirestoreEnums.FIELDS.CURRENCY_CODE.value) as? String ?: "EUR"
                            userPreferencesRepository.updateCurrencyCode(currencyCode)

                            val labelsValue = snapshot.data?.get(FirestoreEnums.FIELDS.LABELS.value) as? List<*>
                            val labels = (labelsValue?.filterIsInstance<String>() ?: emptyList()).sorted()
                            userPreferencesRepository.updateLabels(labels)

                            val proPicChoice = snapshot.data?.get(FirestoreEnums.FIELDS.PRO_PIC_CHOICE.value) as? String
                            if (proPicChoice != null) {
                                userPreferencesRepository.updateProPicChoice(proPicChoice)
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

    override fun stopSnapshotListener() {
        super.stopSnapshotListener()
        rootListener?.remove()
        rootListener = null
    }
}
