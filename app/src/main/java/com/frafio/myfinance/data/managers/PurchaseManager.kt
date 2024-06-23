package com.frafio.myfinance.data.managers

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.setSharedDynamicColor
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate

class PurchaseManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = PurchaseManager::class.java.simpleName
        const val DEFAULT_LIMIT: Long = 50
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun updateList(limit: Long = DEFAULT_LIMIT): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
            .limit(limit)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                PurchaseStorage.populateListFromSnapshot(queryDocumentSnapshots)
                response.value = PurchaseResult(PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS)
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = PurchaseResult(PurchaseCode.PURCHASE_LIST_UPDATE_FAILURE)
            }

        return response
    }

    fun getPurchaseNumber(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
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

    fun deleteAt(position: Int): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(PurchaseStorage.purchaseList[position].id!!).delete()
            .addOnSuccessListener {
                PurchaseStorage.deletePurchaseAt(position)
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
                val totalIndex = PurchaseStorage.addPurchase(purchase)
                response.value = PurchaseResult(
                    PurchaseCode.PURCHASE_ADD_SUCCESS,
                    "${PurchaseCode.PURCHASE_ADD_SUCCESS.message}&$totalIndex"
                )
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_FAILURE)
            }

        return response
    }

    fun editPurchase(
        purchase: Purchase,
        position: Int
    ): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchase.id!!).set(purchase).addOnSuccessListener {
                PurchaseStorage.editPurchaseAt(position, purchase)
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_FAILURE)
            }


        return response
    }

    fun getSumPrices(
        response: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .aggregate(AggregateField.sum(DbPurchases.FIELDS.PRICE.value))
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                val priceSum = snapshot
                    .get(AggregateField.sum(DbPurchases.FIELDS.PRICE.value)) as? Double ?: 0.0
                response.value = Pair(PurchaseCode.PURCHASE_AGGREGATE_SUCCESS, priceSum)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = Pair(PurchaseCode.PURCHASE_AGGREGATE_FAILURE, 0.0)
            }
        return response
    }

    fun getTodayTotal(
        response: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        val todayDate = LocalDate.now()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .whereEqualTo(DbPurchases.FIELDS.DAY.value, todayDate.dayOfMonth)
            .whereEqualTo(DbPurchases.FIELDS.MONTH.value, todayDate.monthValue)
            .whereEqualTo(DbPurchases.FIELDS.YEAR.value, todayDate.year)
            .aggregate(AggregateField.sum(DbPurchases.FIELDS.PRICE.value))
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                val priceSum = snapshot
                    .get(AggregateField.sum(DbPurchases.FIELDS.PRICE.value)) as? Double ?: 0.0
                response.value = Pair(PurchaseCode.PURCHASE_AGGREGATE_SUCCESS, priceSum)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = Pair(PurchaseCode.PURCHASE_AGGREGATE_FAILURE, 0.0)
            }
        return response
    }

    fun getThisMonthTotal(
        response: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        val todayDate = LocalDate.now()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .whereEqualTo(DbPurchases.FIELDS.MONTH.value, todayDate.monthValue)
            .whereEqualTo(DbPurchases.FIELDS.YEAR.value, todayDate.year)
            .aggregate(AggregateField.sum(DbPurchases.FIELDS.PRICE.value))
            .get(AggregateSource.SERVER)
            .addOnSuccessListener { snapshot ->
                val priceSum = snapshot
                    .get(AggregateField.sum(DbPurchases.FIELDS.PRICE.value)) as? Double ?: 0.0
                response.value = Pair(PurchaseCode.PURCHASE_AGGREGATE_SUCCESS, priceSum)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = Pair(PurchaseCode.PURCHASE_AGGREGATE_FAILURE, 0.0)
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