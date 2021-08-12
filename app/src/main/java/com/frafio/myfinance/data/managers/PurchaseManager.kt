package com.frafio.myfinance.data.managers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.auth.AUTH_RESULT
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.storage.PurchaseStorage
import com.frafio.myfinance.data.storage.UserStorage
import com.frafio.myfinance.data.enums.db.DB_PURCHASES
import com.frafio.myfinance.data.models.AuthResult
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

        fStore.collection(DB_PURCHASES.PURCHASES.value)
            .whereEqualTo(DB_PURCHASES.EMAIL.value, UserStorage.user!!.email)
            .orderBy(DB_PURCHASES.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DB_PURCHASES.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DB_PURCHASES.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DB_PURCHASES.TYPE.value)
            .orderBy(DB_PURCHASES.PRICE.value, Query.Direction.DESCENDING)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                PurchaseStorage.resetPurchaseList()

                queryDocumentSnapshots.forEach { document ->
                    val purchase = document.toObject(Purchase::class.java)

                    // set id and update formattedThings
                    purchase.updatePurchase(document.id)

                    PurchaseStorage.purchaseList.add(purchase)
                }

                response.value = AuthResult(AUTH_RESULT.USER_DATA_UPDATED)
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = AuthResult(AUTH_RESULT.USER_DATA_NOT_UPDATED)
            }

        return response
    }
}