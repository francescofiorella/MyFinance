package com.frafio.myfinance.data.managers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.AddCode
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.DbReceipt
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.ReceiptItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReceiptManager {

    companion object {
        private val TAG = ReceiptManager::class.java.simpleName
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun getQuery(purchaseID: String): Query {
        return fStore.collection(DbPurchases.PURCHASES.value).document(purchaseID)
            .collection(DbReceipt.RECEIPT.value).orderBy(DbReceipt.NAME.value)
    }

    fun addItem(receiptItem: ReceiptItem, purchaseID: String): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.PURCHASES.value).document(purchaseID)
            .collection(DbReceipt.RECEIPT.value).add(receiptItem)
            .addOnSuccessListener {
                response.value = PurchaseResult(AddCode.RECEIPT_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(AddCode.RECEIPT_ADD_FAILURE)
            }

        return response
    }

    fun deleteItem(receiptItem: ReceiptItem, purchaseID: String): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.PURCHASES.value).document(purchaseID)
            .collection(DbReceipt.RECEIPT.value).document(receiptItem.id!!).delete()
            .addOnSuccessListener {
                response.value = PurchaseResult(AddCode.RECEIPT_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(AddCode.RECEIPT_DELETE_FAILURE)
            }

        return response
    }
}