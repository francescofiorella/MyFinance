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
import com.frafio.myfinance.utils.getSharedCategory
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.setSharedCategory
import com.frafio.myfinance.utils.setSharedDynamicColor
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate

class PurchaseManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = PurchaseManager::class.java.simpleName
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun getCategories(): LiveData<Pair<PurchaseResult, List<String>>> {
        val response = MutableLiveData<Pair<PurchaseResult, List<String>>>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .get().addOnSuccessListener { docSnap ->
                val categories =
                    (docSnap.data?.get(DbPurchases.FIELDS.CATEGORIES.value) as List<*>).map { value ->
                        value.toString()
                    }
                response.value = Pair(
                    PurchaseResult(PurchaseCode.PURCHASE_GET_CATEGORIES_SUCCESS),
                    categories
                )
            }.addOnFailureListener {
                response.value = Pair(
                    PurchaseResult(PurchaseCode.PURCHASE_GET_CATEGORIES_FAILURE),
                    listOf()
                )
            }

        return response
    }

    fun createCategory(name: String): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .get().addOnSuccessListener { docSnap ->
                val categories =
                    (docSnap.data?.get(DbPurchases.FIELDS.CATEGORIES.value) as List<*>).map { value ->
                        value.toString()
                    }
                val mutCat = categories.toMutableList()
                mutCat.add(name)
                fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                    .document(UserStorage.user!!.email!!)
                    .update(DbPurchases.FIELDS.CATEGORIES.value, mutCat).addOnSuccessListener {
                        response.value =
                            PurchaseResult(PurchaseCode.PURCHASE_CREATE_CATEGORY_SUCCESS)
                    }.addOnFailureListener {
                        response.value =
                            PurchaseResult(PurchaseCode.PURCHASE_CREATE_CATEGORY_FAILURE)
                    }
            }.addOnFailureListener {
                response.value = PurchaseResult(PurchaseCode.PURCHASE_CREATE_CATEGORY_FAILURE)
            }
        return response
    }

    fun updateList(limit: Long = 30): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .whereEqualTo(DbPurchases.FIELDS.CATEGORY.value, getSharedCategory(sharedPreferences))
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
            .whereEqualTo(DbPurchases.FIELDS.CATEGORY.value, getSharedCategory(sharedPreferences))
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
                purchase.updateID(it.id)
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
            .whereEqualTo(DbPurchases.FIELDS.CATEGORY.value, getSharedCategory(sharedPreferences))
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
            .whereEqualTo(DbPurchases.FIELDS.CATEGORY.value, getSharedCategory(sharedPreferences))
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
            .whereEqualTo(DbPurchases.FIELDS.CATEGORY.value, getSharedCategory(sharedPreferences))
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

    fun updateListByCollection(collection: String): LiveData<PurchaseResult> {
        setSharedCategory(sharedPreferences, collection)
        return updateList()
    }

    fun getSelectedCategory(): String {
        return getSharedCategory(sharedPreferences)
    }

    fun setDynamicColorActive(active: Boolean) {
        setSharedDynamicColor(sharedPreferences, active)
    }

    fun getDynamicColorActive(): Boolean {
        return getSharedDynamicColor(sharedPreferences)
    }
}