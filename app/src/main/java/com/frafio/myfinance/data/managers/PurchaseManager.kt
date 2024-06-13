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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

    fun updateList(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .whereEqualTo(
                DbPurchases.FIELDS.CATEGORY.value,
                getSharedCategory(sharedPreferences)
            )
            .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
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

    fun deleteAt(position: Int): LiveData<Triple<PurchaseResult, List<Purchase>, Int?>> {
        val response = MutableLiveData<Triple<PurchaseResult, List<Purchase>, Int?>>()
        var totPosition: Int

        val purchaseList = PurchaseStorage.purchaseList.toMutableList()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchaseList[position].id!!).delete()
            .addOnSuccessListener {
                if (purchaseList[position].type != DbPurchases.TYPES.TRANSPORT.value
                    && purchaseList[position].type != DbPurchases.TYPES.RENT.value
                ) {
                    for (i in position - 1 downTo 0) {
                        if (purchaseList[i].type == DbPurchases.TYPES.TOTAL.value) {
                            totPosition = i

                            val newTotal = purchaseList[totPosition]
                            newTotal.price = newTotal.price?.minus(
                                purchaseList[position].price!!
                            )

                            purchaseList.removeAt(position)
                            val todayDate = LocalDate.now()
                            val totalDate = LocalDate.of(
                                newTotal.year!!,
                                newTotal.month!!,
                                newTotal.day!!
                            )
                            if (ChronoUnit.DAYS.between(totalDate, todayDate) >= 0) {
                                purchaseList[totPosition] = newTotal
                            } else if (newTotal.price == 0.0) {
                                purchaseList.removeAt(totPosition)
                            }

                            PurchaseStorage.purchaseList = purchaseList

                            response.value = Triple(
                                PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS),
                                purchaseList,
                                totPosition
                            )
                            break
                        }
                    }
                } else {
                    purchaseList.removeAt(position)
                    PurchaseStorage.purchaseList = purchaseList

                    response.value = Triple(
                        PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS),
                        purchaseList,
                        null
                    )
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value =
                    Triple(PurchaseResult(PurchaseCode.PURCHASE_DELETE_FAILURE), purchaseList, null)
            }

        return response
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .add(purchase).addOnSuccessListener {
                response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_SUCCESS)
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
                PurchaseStorage.purchaseList[position] = purchase
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_FAILURE)
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