package com.frafio.myfinance.core.data.manager

import android.util.Log
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
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
