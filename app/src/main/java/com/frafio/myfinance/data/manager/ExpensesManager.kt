package com.frafio.myfinance.data.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.data.storage.MyFinanceStorage
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.getSharedMonthlyBudget
import com.frafio.myfinance.utils.setSharedDynamicColor
import com.frafio.myfinance.utils.setSharedMonthlyBudget
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpensesManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = ExpensesManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 100
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()
    private val expensesLocalRepository = ExpensesLocalRepository()

    fun getMonthlyBudget(): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()
        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!).get()
            .addOnSuccessListener {
                val value = it.data?.get(FirestoreEnums.FIELDS.MONTHLY_BUDGET.value).toString()
                    .toDoubleOrNull() ?: 0.0
                setLocalMonthlyBudget(value)
                MyFinanceStorage.updateBudget(value)
                response.value = FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
            }
            .addOnFailureListener {
                response.value = FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
            }
        return response
    }

    fun setMonthlyBudget(budget: Double): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()
        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .set(hashMapOf(FirestoreEnums.FIELDS.MONTHLY_BUDGET.value to budget))
            .addOnSuccessListener {
                setLocalMonthlyBudget(budget)
                MyFinanceStorage.updateBudget(budget)
                response.value = FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
            }
            .addOnFailureListener {
                response.value = FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
            }
        return response
    }

    fun updateExpensesList(): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()

        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
            val expenseList = mutableListOf<Expense>()
            queryDocumentSnapshots.forEach { document ->
                val expense = document.toObject(Expense::class.java)
                // set id
                expense.id = document.id
                expenseList.add(expense)
            }
            CoroutineScope(Dispatchers.IO).launch {
                expensesLocalRepository.updateTable(expenseList)
            }
            response.value = FinanceResult(FinanceCode.EXPENSE_LIST_UPDATE_SUCCESS)
        }.addOnFailureListener { e ->
            val error = "Error! ${e.localizedMessage}"
            Log.e(TAG, error)

            response.value = FinanceResult(FinanceCode.EXPENSE_LIST_UPDATE_FAILURE)
        }

        return response
    }

    fun addExpenses(expense: Expense): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()

        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
            .add(expense).addOnSuccessListener {
                expense.id = it.id
                CoroutineScope(Dispatchers.IO).launch {
                    expensesLocalRepository.insertExpense(expense)
                }
                response.value = FinanceResult(FinanceCode.EXPENSE_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = FinanceResult(FinanceCode.EXPENSE_ADD_FAILURE)
            }

        return response
    }

    fun editExpense(expense: Expense): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()

        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
            .document(expense.id).set(expense).addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    expensesLocalRepository.updateExpense(expense)
                }
                response.value = FinanceResult(FinanceCode.EXPENSE_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
            }


        return response
    }

    fun deleteExpense(expense: Expense): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()
        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.PAYMENTS.value)
            .document(expense.id).delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    expensesLocalRepository.deleteExpense(expense)
                }
                response.value = FinanceResult(FinanceCode.EXPENSE_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value = FinanceResult(FinanceCode.EXPENSE_DELETE_FAILURE)
            }

        return response
    }

    fun setDynamicColorActive(active: Boolean) {
        setSharedDynamicColor(sharedPreferences, active)
    }

    fun getDynamicColorActive(): Boolean {
        return getSharedDynamicColor(sharedPreferences)
    }

    private fun setLocalMonthlyBudget(value: Double) {
        setSharedMonthlyBudget(sharedPreferences, value)
    }

    fun updateLocalMonthlyBudget() {
        MyFinanceStorage.updateBudget(getSharedMonthlyBudget(sharedPreferences))
    }
}