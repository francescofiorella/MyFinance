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

    fun updateList(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .whereEqualTo(DbPurchases.FIELDS.EMAIL.value, UserStorage.user!!.email)
            .whereEqualTo(DbPurchases.FIELDS.CATEGORY.value, getSharedCategory(sharedPreferences))
            .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.TYPE.value)
            .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                PurchaseStorage.resetPurchaseList()

                queryDocumentSnapshots.forEach { document ->
                    val purchase = document.toObject(Purchase::class.java)

                    // set id and update formattedThings
                    purchase.updateID(document.id)

                    PurchaseStorage.purchaseList.add(purchase)
                }

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
                if (purchaseList[position].type == DbPurchases.TYPES.TOTAL.value) {
                    purchaseList.removeAt(position)
                    PurchaseStorage.purchaseList = purchaseList

                    response.value = Triple(
                        PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS),
                        purchaseList,
                        null
                    )
                } else if (purchaseList[position].type != DbPurchases.TYPES.TRANSPORT.value
                    && purchaseList[position].type != DbPurchases.TYPES.RENT.value
                ) {
                    for (i in position - 1 downTo 0) {
                        if (purchaseList[i].type == DbPurchases.TYPES.TOTAL.value) {
                            totPosition = i

                            val newPurchase = purchaseList[totPosition]
                            newPurchase.price = newPurchase.price?.minus(
                                purchaseList[position].price!!
                            )

                            purchaseList[totPosition] = newPurchase
                            purchaseList.removeAt(position)

                            fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                                .document(UserStorage.user!!.email!!)
                                .collection(DbPurchases.FIELDS.PAYMENTS.value)
                                .document(purchaseList[i].id!!)
                                .set(purchaseList[i]).addOnSuccessListener {
                                    PurchaseStorage.purchaseList = purchaseList

                                    response.value = Triple(
                                        PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS),
                                        purchaseList,
                                        totPosition
                                    )
                                }.addOnFailureListener { e ->
                                    Log.e(TAG, "Error! ${e.localizedMessage}")

                                    response.value = Triple(
                                        PurchaseResult(PurchaseCode.PURCHASE_DELETE_FAILURE),
                                        purchaseList,
                                        totPosition
                                    )
                                }
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

    fun addTotal(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .add(purchase).addOnSuccessListener {
                response.value = PurchaseResult(PurchaseCode.TOTAL_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.TOTAL_ADD_FAILURE)
            }

        return response
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        val userEmail = UserStorage.user!!.email

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .add(purchase).addOnSuccessListener {
                var sum =
                    if (purchase.type != DbPurchases.TYPES.TRANSPORT.value
                        && purchase.type != DbPurchases.TYPES.RENT.value
                    ) {
                        purchase.price ?: 0.0
                    } else {
                        0.0
                    }
                for (item in PurchaseStorage.purchaseList) {
                    if (item.email == userEmail
                        && item.type != DbPurchases.TYPES.TOTAL.value
                        && item.type != DbPurchases.TYPES.TRANSPORT.value
                        && item.type != DbPurchases.TYPES.RENT.value
                        && item.year == purchase.year
                        && item.month == purchase.month
                        && item.day == purchase.day
                    ) {
                        sum += item.price ?: 0.0
                    }
                }
                fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                    .document(UserStorage.user!!.email!!)
                    .collection(DbPurchases.FIELDS.PAYMENTS.value)
                    .whereEqualTo(DbPurchases.FIELDS.TYPE.value, DbPurchases.TYPES.TOTAL.value)
                    .whereEqualTo(
                        DbPurchases.FIELDS.CATEGORY.value,
                        getSharedCategory(sharedPreferences)
                    )
                    .whereEqualTo(DbPurchases.FIELDS.DAY.value, purchase.day)
                    .whereEqualTo(DbPurchases.FIELDS.MONTH.value, purchase.month)
                    .whereEqualTo(DbPurchases.FIELDS.YEAR.value, purchase.year)
                    .get().addOnSuccessListener { queryDocumentSnapshots ->
                        if (queryDocumentSnapshots.size() == 1) {
                            val totalP = queryDocumentSnapshots.documents[0]
                                .toObject(Purchase::class.java)!!
                            totalP.updateID(queryDocumentSnapshots.documents[0].id)
                            totalP.price = sum
                            fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                                .document(UserStorage.user!!.email!!)
                                .collection(DbPurchases.FIELDS.PAYMENTS.value)
                                .document(totalP.id!!).set(totalP)
                                .addOnSuccessListener {
                                    response.value = PurchaseResult(PurchaseCode.TOTAL_ADD_SUCCESS)
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error! ${e.localizedMessage}")
                                    response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_ERROR)
                                }
                        } else if (queryDocumentSnapshots.size() == 0) {
                            val totalP = Purchase(
                                userEmail,
                                DbPurchases.NAMES.TOTAL.value,
                                sum,
                                purchase.year,
                                purchase.month,
                                purchase.day,
                                DbPurchases.TYPES.TOTAL.value,
                                category = getSharedCategory(sharedPreferences)
                            )
                            fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                                .document(UserStorage.user!!.email!!)
                                .collection(DbPurchases.FIELDS.PAYMENTS.value)
                                .add(totalP).addOnSuccessListener {
                                    response.value = PurchaseResult(PurchaseCode.TOTAL_ADD_SUCCESS)
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error! ${e.localizedMessage}")
                                    response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_ERROR)
                                }
                        } else {
                            response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_FAILURE)
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error! ${e.localizedMessage}")
                        response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_FAILURE)
                    }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_FAILURE)
            }

        return response
    }

    fun editPurchase(
        purchase: Purchase,
        position: Int,
        purchasePrice: Double
    ): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchase.id!!).set(purchase).addOnSuccessListener {
                PurchaseStorage.purchaseList[position] = purchase
                if (purchase.price != purchasePrice) {
                    var sum = 0.0
                    for (item in PurchaseStorage.purchaseList) {
                        if (item.email == purchase.email
                            && item.type != DbPurchases.TYPES.TOTAL.value
                            && item.type != DbPurchases.TYPES.TRANSPORT.value
                            && item.type != DbPurchases.TYPES.RENT.value
                            && item.year == purchase.year
                            && item.month == purchase.month
                            && item.day == purchase.day
                        ) {
                            sum += item.price ?: 0.0
                        }
                    }

                    fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                        .document(UserStorage.user!!.email!!)
                        .collection(DbPurchases.FIELDS.PAYMENTS.value)
                        .whereEqualTo(DbPurchases.FIELDS.TYPE.value, DbPurchases.TYPES.TOTAL.value)
                        .whereEqualTo(
                            DbPurchases.FIELDS.CATEGORY.value,
                            getSharedCategory(sharedPreferences)
                        )
                        .whereEqualTo(DbPurchases.FIELDS.DAY.value, purchase.day)
                        .whereEqualTo(DbPurchases.FIELDS.MONTH.value, purchase.month)
                        .whereEqualTo(DbPurchases.FIELDS.YEAR.value, purchase.year)
                        .get().addOnSuccessListener { queryDocumentSnapshots ->
                            if (queryDocumentSnapshots.size() == 1) {
                                val totalP: Purchase = queryDocumentSnapshots.documents[0]
                                    .toObject(Purchase::class.java)!!
                                totalP.updateID(queryDocumentSnapshots.documents[0].id)
                                totalP.price = sum
                                fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                                    .document(UserStorage.user!!.email!!)
                                    .collection(DbPurchases.FIELDS.PAYMENTS.value)
                                    .document(totalP.id!!).set(totalP)
                                    .addOnSuccessListener {
                                        response.value =
                                            PurchaseResult(PurchaseCode.TOTAL_ADD_SUCCESS)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error! ${e.localizedMessage}")
                                        response.value =
                                            PurchaseResult(PurchaseCode.PURCHASE_ADD_ERROR)
                                    }
                            }
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "Error! ${e.localizedMessage}")
                            response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_ERROR)
                        }
                } else {
                    response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_SUCCESS)
                }
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