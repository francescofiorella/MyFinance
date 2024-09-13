package com.frafio.myfinance.data.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.repository.IncomesLocalRepository
import com.frafio.myfinance.data.storage.MyFinanceStorage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IncomesManager {

    companion object {
        private val TAG = IncomesManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 100
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val incomesLocalRepository = IncomesLocalRepository()

    fun updateIncomeList(): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()
        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.INCOMES.value)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                val incomeList = mutableListOf<Income>()
                queryDocumentSnapshots.forEach { document ->
                    val income = document.toObject(Income::class.java)
                    // set id
                    income.id = document.id
                    incomeList.add(income)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    incomesLocalRepository.updateTable(incomeList)
                }
                response.value = FinanceResult(FinanceCode.INCOME_LIST_UPDATE_SUCCESS)
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)
                response.value = FinanceResult(FinanceCode.INCOME_LIST_UPDATE_FAILURE)
            }
        return response
    }

    fun addIncome(income: Income): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()

        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.INCOMES.value)
            .add(income).addOnSuccessListener {
                income.id = it.id
                CoroutineScope(Dispatchers.IO).launch {
                    incomesLocalRepository.insertIncome(income)
                }
                response.value = FinanceResult(FinanceCode.INCOME_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = FinanceResult(FinanceCode.INCOME_ADD_FAILURE)
            }

        return response
    }

    fun editIncome(income: Income): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()

        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.INCOMES.value)
            .document(income.id).set(income).addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    incomesLocalRepository.updateIncome(income)
                }
                response.value = FinanceResult(FinanceCode.INCOME_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = FinanceResult(FinanceCode.INCOME_EDIT_FAILURE)
            }


        return response
    }

    fun deleteIncome(income: Income): LiveData<FinanceResult> {
        val response = MutableLiveData<FinanceResult>()
        fStore.collection(FirestoreEnums.FIELDS.PURCHASES.value)
            .document(MyFinanceStorage.user!!.email!!)
            .collection(FirestoreEnums.FIELDS.INCOMES.value)
            .document(income.id).delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    incomesLocalRepository.deleteIncome(income)
                }
                response.value = FinanceResult(FinanceCode.INCOME_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value = FinanceResult(FinanceCode.INCOME_DELETE_FAILURE)
            }

        return response
    }
}