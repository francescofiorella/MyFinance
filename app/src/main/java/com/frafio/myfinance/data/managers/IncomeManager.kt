package com.frafio.myfinance.data.managers

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Income
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.IncomeStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class IncomeManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = IncomeManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 50
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun updateIncomeList(limit: Long = DEFAULT_LIMIT): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.INCOMES.value)
            .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
            .limit(limit).get()
            .addOnSuccessListener { incomesSnapshot ->
                IncomeStorage.populateIncomesFromSnapshot(incomesSnapshot)
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
                val totalIndex = IncomeStorage.addIncome(income)
                response.value = PurchaseResult(
                    PurchaseCode.INCOME_ADD_SUCCESS,
                    "${PurchaseCode.INCOME_ADD_SUCCESS.message}&$totalIndex"
                )
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INCOME_ADD_FAILURE)
            }

        return response
    }

    fun deleteIncomeAt(position: Int): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.INCOMES.value)
            .document(IncomeStorage.incomeList[position].id).delete()
            .addOnSuccessListener {
                IncomeStorage.deleteIncomeAt(position)
                response.value = PurchaseResult(PurchaseCode.INCOME_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value = PurchaseResult(PurchaseCode.INCOME_DELETE_FAILURE)
            }

        return response
    }

    fun editIncome(income: Income, position: Int): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.INCOMES.value)
            .document(income.id).set(income).addOnSuccessListener {
                IncomeStorage.deleteIncomeAt(position)
                IncomeStorage.addIncome(income)
                response.value = PurchaseResult(PurchaseCode.INCOME_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INCOME_EDIT_FAILURE)
            }


        return response
    }
}