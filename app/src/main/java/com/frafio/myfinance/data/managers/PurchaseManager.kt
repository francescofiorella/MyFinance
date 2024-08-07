package com.frafio.myfinance.data.managers

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.repositories.LocalPurchaseRepository
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.getSharedMonthlyBudget
import com.frafio.myfinance.utils.setSharedDynamicColor
import com.frafio.myfinance.utils.setSharedMonthlyBudget
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PurchaseManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = PurchaseManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 100
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()
    private val localPurchaseRepository = LocalPurchaseRepository()

    fun getMonthlyBudget(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!).get()
            .addOnSuccessListener {
                val value = it.data?.get(DbPurchases.FIELDS.MONTHLY_BUDGET.value).toString()
                    .toDoubleOrNull() ?: 0.0
                setLocalMonthlyBudget(value)
                PurchaseStorage.updateBudget(value)
                response.value = PurchaseResult(PurchaseCode.BUDGET_UPDATE_SUCCESS)
            }
            .addOnFailureListener {
                response.value = PurchaseResult(PurchaseCode.BUDGET_UPDATE_FAILURE)
            }
        return response
    }

    fun setMonthlyBudget(budget: Double): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .set(hashMapOf(DbPurchases.FIELDS.MONTHLY_BUDGET.value to budget))
            .addOnSuccessListener {
                setLocalMonthlyBudget(budget)
                PurchaseStorage.updateBudget(budget)
                response.value = PurchaseResult(PurchaseCode.BUDGET_UPDATE_SUCCESS)
            }
            .addOnFailureListener {
                response.value = PurchaseResult(PurchaseCode.BUDGET_UPDATE_FAILURE)
            }
        return response
    }

    fun updatePurchaseList(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
            val purchaseList = mutableListOf<Purchase>()
            queryDocumentSnapshots.forEach { document ->
                val purchase = document.toObject(Purchase::class.java)
                // set id
                purchase.id = document.id
                purchaseList.add(purchase)
            }
            CoroutineScope(Dispatchers.IO).launch {
                localPurchaseRepository.updateTable(purchaseList)
            }
            response.value = PurchaseResult(PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS)
        }.addOnFailureListener { e ->
            val error = "Error! ${e.localizedMessage}"
            Log.e(TAG, error)

            response.value = PurchaseResult(PurchaseCode.PURCHASE_LIST_UPDATE_FAILURE)
        }

        return response
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .add(purchase).addOnSuccessListener {
                purchase.id = it.id
                CoroutineScope(Dispatchers.IO).launch {
                    localPurchaseRepository.insertPurchase(purchase)
                }
                response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_FAILURE)
            }

        return response
    }

    fun editPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchase.id).set(purchase).addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    localPurchaseRepository.updatePurchase(purchase)
                }
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_FAILURE)
            }


        return response
    }

    fun deletePurchase(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchase.id).delete()
            .addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    localPurchaseRepository.deletePurchase(purchase)
                }
                response.value = PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value = PurchaseResult(PurchaseCode.PURCHASE_DELETE_FAILURE)
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
        PurchaseStorage.updateBudget(getSharedMonthlyBudget(sharedPreferences))
    }
}