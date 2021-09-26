package com.frafio.myfinance.data.managers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PurchaseManager {

    companion object {
        private val TAG = PurchaseManager::class.java.simpleName
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun updateList(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
            .whereEqualTo(DbPurchases.FIELDS.EMAIL.value, UserStorage.user!!.email)
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

    fun deleteAt(position: Int): LiveData<Triple<PurchaseResult, Int?, Int?>> {
        val response = MutableLiveData<Triple<PurchaseResult, Int?, Int?>>()
        var totPosition: Int

        val purchaseList = PurchaseStorage.purchaseList

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
            .document(purchaseList[position].id!!).delete()
            .addOnSuccessListener {
                if (purchaseList[position].type == DbPurchases.TYPES.TOTAL.value) {
                    purchaseList.removeAt(position)
                    PurchaseStorage.purchaseList = purchaseList

                    response.value =
                        Triple(PurchaseResult(PurchaseCode.TOTAL_DELETE_SUCCESS), position, null)
                } else if (purchaseList[position].type != DbPurchases.TYPES.TICKET.value) {
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
                                .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
                                .document(purchaseList[i].id!!)
                                .set(purchaseList[i]).addOnSuccessListener {
                                    PurchaseStorage.purchaseList = purchaseList

                                    response.value =
                                        Triple(
                                            PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS),
                                            position,
                                            totPosition
                                        )
                                }.addOnFailureListener { e ->
                                    Log.e(TAG, "Error! ${e.localizedMessage}")

                                    response.value = Triple(
                                        PurchaseResult(PurchaseCode.PURCHASE_DELETE_FAILURE),
                                        null,
                                        null
                                    )
                                }
                            break
                        }
                    }
                } else {
                    purchaseList.removeAt(position)
                    PurchaseStorage.purchaseList = purchaseList

                    response.value =
                        Triple(PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS), position, null)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value =
                    Triple(PurchaseResult(PurchaseCode.PURCHASE_DELETE_FAILURE), null, null)
            }

        return response
    }

    fun addTotale(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        purchase.id = "${purchase.year}${purchase.month}${purchase.day}"
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
            .document(purchase.id!!).set(purchase).addOnSuccessListener {
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
            .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
            .add(purchase).addOnSuccessListener {
                var sum = if (purchase.type != DbPurchases.TYPES.TICKET.value) {
                    purchase.price ?: 0.0
                } else {
                    0.0
                }
                for (item in PurchaseStorage.purchaseList) {
                    if (item.email == userEmail && item.type != DbPurchases.TYPES.TOTAL.value
                        && item.type != DbPurchases.TYPES.TICKET.value && item.year == purchase.year
                        && item.month == purchase.month && item.day == purchase.day
                    ) {
                        sum += item.price ?: 0.0
                    }
                }
                val totalP = Purchase(
                    userEmail,
                    DbPurchases.NAMES.TOTALE.value,
                    sum,
                    purchase.year,
                    purchase.month,
                    purchase.day,
                    DbPurchases.TYPES.TOTAL.value
                )
                totalP.id = "${purchase.year}${purchase.month}${purchase.day}"
                fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                    .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
                    .document(totalP.id!!).set(totalP)
                    .addOnSuccessListener {
                        response.value = PurchaseResult(PurchaseCode.TOTAL_ADD_SUCCESS)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error! ${e.localizedMessage}")
                        response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_ERROR)
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
            .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
            .document(purchase.id!!).set(purchase).addOnSuccessListener {
                PurchaseStorage.purchaseList[position] = purchase
                if (purchase.price != purchasePrice) {
                    var sum = 0.0
                    for (item in PurchaseStorage.purchaseList) {
                        if (item.email == purchase.email && item.type != DbPurchases.TYPES.TOTAL.value
                            && item.type != DbPurchases.TYPES.TICKET.value && item.year == purchase.year
                            && item.month == purchase.month && item.day == purchase.day
                        ) {
                            sum += item.price ?: 0.0
                        }
                    }
                    val totID = "${purchase.year}${purchase.month}${purchase.day}"
                    val totalP = Purchase(
                        purchase.email,
                        DbPurchases.NAMES.TOTALE.value,
                        sum,
                        purchase.year,
                        purchase.month,
                        purchase.day,
                        DbPurchases.TYPES.TOTAL.value,
                        totID
                    )

                    fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                        .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.UNO_DUE.value)
                        .document(totID).set(totalP)
                        .addOnSuccessListener {
                            response.value = PurchaseResult(PurchaseCode.TOTAL_ADD_SUCCESS)
                        }
                        .addOnFailureListener { e ->
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
}