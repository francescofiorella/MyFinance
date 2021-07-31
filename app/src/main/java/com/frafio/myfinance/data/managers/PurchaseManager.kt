package com.frafio.myfinance.data.managers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.storage.PurchaseStorage
import com.frafio.myfinance.data.storage.UserStorage
import com.frafio.myfinance.data.db_enums.DbPurchases
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PurchaseManager {

    companion object {
        private val TAG = PurchaseManager::class.java.simpleName

        const val LIST_UPDATED: Int = 10
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun updateList(): LiveData<Any> {
        val response = MutableLiveData<Any>()

        fStore.collection(DbPurchases.PURCHASES.value)
            .whereEqualTo(DbPurchases.EMAIL.value, UserStorage.user!!.email)
            .orderBy(DbPurchases.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.TYPE.value)
            .orderBy(DbPurchases.PRICE.value, Query.Direction.DESCENDING)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                PurchaseStorage.resetPurchaseList()

                queryDocumentSnapshots.forEach { document ->
                    val purchase = document.toObject(Purchase::class.java)

                    // set id and update formattedThings
                    purchase.updatePurchase(document.id)

                    PurchaseStorage.purchaseList.add(purchase)
                }

                response.value = LIST_UPDATED
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = "Error! ${e.localizedMessage}"
            }

        return response
    }
}