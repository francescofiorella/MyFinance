package com.frafio.myfinance.data.manager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.PurchaseResult
import com.frafio.myfinance.data.repository.LocalIncomeRepository
import com.frafio.myfinance.data.storage.UserStorage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IncomeManager {

    companion object {
        private val TAG = IncomeManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 100
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val localIncomeRepository = LocalIncomeRepository()

    fun updateIncomeList(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.INCOMES.value)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                val incomeList = mutableListOf<Income>()
                queryDocumentSnapshots.forEach { document ->
                    val income = document.toObject(Income::class.java)
                    // set id
                    income.id = document.id
                    incomeList.add(income)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    localIncomeRepository.updateTable(incomeList)
                }
                response.value = PurchaseResult(PurchaseCode.INCOME_LIST_UPDATE_SUCCESS)
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)
                response.value = PurchaseResult(PurchaseCode.INCOME_LIST_UPDATE_FAILURE)
            }
        return response
    }

    fun addIncome(income: Income): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.INCOMES.value)
            .add(income).addOnSuccessListener {
                income.id = it.id
                CoroutineScope(Dispatchers.IO).launch {
                    localIncomeRepository.insertIncome(income)
                }
                response.value = PurchaseResult(PurchaseCode.INCOME_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INCOME_ADD_FAILURE)
            }

        return response
    }

    fun editIncome(income: Income): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.INCOMES.value)
            .document(income.id).set(income).addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    localIncomeRepository.updateIncome(income)
                }
                response.value = PurchaseResult(PurchaseCode.INCOME_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INCOME_EDIT_FAILURE)
            }


        return response
    }

    fun deleteIncome(income: Income): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.INCOMES.value)
            .document(income.id).delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    localIncomeRepository.deleteIncome(income)
                }
                response.value = PurchaseResult(PurchaseCode.INCOME_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value = PurchaseResult(PurchaseCode.INCOME_DELETE_FAILURE)
            }

        return response
    }
}