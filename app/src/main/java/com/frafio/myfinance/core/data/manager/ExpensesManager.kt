package com.frafio.myfinance.core.data.manager

import android.util.Log
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val expensesLocalRepository: ExpensesLocalRepository
) {

    companion object {
        private val TAG = ExpensesManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 50
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private suspend fun getUserEmail(): String? {
        return userPreferencesRepository.userPreferencesFlow.first().user?.email
    }

    suspend fun getMonthlyBudget(): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        return@withContext try {
            val document = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email).get().await()
            val value = document.data?.get(FirestoreEnums.FIELDS.MONTHLY_BUDGET.value).toString()
                .toDoubleOrNull() ?: 0.0
            userPreferencesRepository.updateMonthlyBudget(value)
            FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
        } catch (_: Exception) {
            FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        }
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

    suspend fun getLabels(): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        return@withContext try {
            val document = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email).get().await()
            val value = document.data?.get(FirestoreEnums.FIELDS.LABELS.value) as? List<*>
            val labels = (value?.filterIsInstance<String>() ?: emptyList()).sorted()
            userPreferencesRepository.updateLabels(labels)
            FinanceResult(FinanceCode.LABELS_UPDATE_SUCCESS)
        } catch (_: Exception) {
            FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
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
            editExpense(updatedExpense)
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
            editExpense(expense)
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
            editExpense(updatedExpense)
        }
        result
    }

    suspend fun updateExpensesList(): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.EXPENSE_LIST_UPDATE_FAILURE)
        return@withContext try {
            val documentSnapshot = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email).get().await()
            
            val labelsValue = documentSnapshot.data?.get(FirestoreEnums.FIELDS.LABELS.value) as? List<*>
            val labels = (labelsValue?.filterIsInstance<String>() ?: emptyList()).sorted()
            userPreferencesRepository.updateLabels(labels)

            val queryDocumentSnapshots = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
                .get().await()

            val expenseList = mutableListOf<Expense>()
            queryDocumentSnapshots.forEach { document ->
                var expense = document.toObject(Expense::class.java)
                expense.id = document.id
                val newLabels = expense.labels.toMutableList()
                for (label in expense.labels) {
                    if (!labels.contains(label)) {
                        newLabels.remove(label)
                    }
                }
                if (newLabels.size != expense.labels.size) {
                    val updatedExpense = expense.copy(labels = newLabels)
                    editExpense(updatedExpense)
                    expense = updatedExpense
                }

                expenseList.add(expense)
            }
            expensesLocalRepository.updateTable(expenseList)
            FinanceResult(FinanceCode.EXPENSE_LIST_UPDATE_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            FinanceResult(FinanceCode.EXPENSE_LIST_UPDATE_FAILURE)
        }
    }

    suspend fun addExpenses(expense: Expense): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.EXPENSE_ADD_FAILURE)
        return@withContext try {
            val documentReference = fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
                .add(expense).await()
            expense.id = documentReference.id
            expensesLocalRepository.insertExpense(expense)
            FinanceResult(FinanceCode.EXPENSE_ADD_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            FinanceResult(FinanceCode.EXPENSE_ADD_FAILURE)
        }
    }

    suspend fun editExpense(expense: Expense): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
                .document(expense.id).set(expense).await()
            expensesLocalRepository.updateExpense(expense)
            FinanceResult(FinanceCode.EXPENSE_EDIT_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
        }
    }

    suspend fun deleteExpense(expense: Expense): FinanceResult = withContext(Dispatchers.IO) {
        val email = getUserEmail() ?: return@withContext FinanceResult(FinanceCode.EXPENSE_DELETE_FAILURE)
        return@withContext try {
            fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
                .document(email)
                .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
                .document(expense.id).delete().await()
            expensesLocalRepository.deleteExpense(expense)
            FinanceResult(FinanceCode.EXPENSE_DELETE_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            FinanceResult(FinanceCode.EXPENSE_DELETE_FAILURE)
        }
    }

    suspend fun setDynamicColorActive(active: Boolean) {
        userPreferencesRepository.updateDynamicColor(active)
    }
}
