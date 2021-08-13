package com.frafio.myfinance.data.managers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.storage.PurchaseStorage
import com.frafio.myfinance.data.storage.UserStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PurchaseManager {

    companion object {
        private val TAG = PurchaseManager::class.java.simpleName
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun updateList(): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

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

                response.value = AuthResult(AuthCode.USER_DATA_UPDATED)
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = AuthResult(AuthCode.USER_DATA_NOT_UPDATED)
            }

        return response
    }
}