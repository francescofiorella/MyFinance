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
import com.frafio.myfinance.utils.setSharedDynamicColor
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
                PurchaseStorage.updateBudget(
                    it.data?.get(DbPurchases.FIELDS.MONTHLY_BUDGET.value).toString()
                        .toDoubleOrNull() ?: 0.0
                )
                response.value = PurchaseResult(PurchaseCode.BUDGET_UPDATE_SUCCESS)
            }
            .addOnFailureListener {
                response.value = PurchaseResult(PurchaseCode.BUDGET_UPDATE_FAILURE)
            }
        return response
    }

    fun updateMonthlyBudget(budget: Double): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .set(hashMapOf(DbPurchases.FIELDS.MONTHLY_BUDGET.value to budget))
            .addOnSuccessListener {
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

        val query = fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
        query.get().addOnSuccessListener { queryDocumentSnapshots ->
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

    fun getPurchaseNumber(
        collection: String = DbPurchases.FIELDS.PAYMENTS.value
    ): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(collection)
            .count()
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                response.value = PurchaseResult(
                    PurchaseCode.PURCHASE_COUNT_SUCCESS,
                    snapshot.count.toString()
                )
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = PurchaseResult(PurchaseCode.PURCHASE_COUNT_FAILURE)
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
                // Check if today empty works
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

    fun setDynamicColorActive(active: Boolean) {
        setSharedDynamicColor(sharedPreferences, active)
    }

    fun getDynamicColorActive(): Boolean {
        return getSharedDynamicColor(sharedPreferences)
    }
}