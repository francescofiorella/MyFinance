package com.frafio.myfinance.data.managers

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.DbReceipt
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.data.storages.UserStorage
import com.frafio.myfinance.utils.getSharedCollection
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class InvoiceManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = InvoiceManager::class.java.simpleName
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun getQuery(purchaseID: String): Query {
        return fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(getSharedCollection(sharedPreferences))
            .document(purchaseID).collection(DbReceipt.FIELDS.RECEIPT.value)
            .orderBy(DbReceipt.FIELDS.NAME.value)
    }

    fun addItem(invoiceItem: InvoiceItem, purchaseID: String): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(getSharedCollection(sharedPreferences))
            .document(purchaseID).collection(DbReceipt.FIELDS.RECEIPT.value)
            .add(invoiceItem).addOnSuccessListener {
                response.value = PurchaseResult(PurchaseCode.INVOICE_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INVOICE_ADD_FAILURE)
            }

        return response
    }

    fun deleteItem(invoiceItem: InvoiceItem, purchaseID: String): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(getSharedCollection(sharedPreferences))
            .document(purchaseID).collection(DbReceipt.FIELDS.RECEIPT.value)
            .document(invoiceItem.id!!).delete().addOnSuccessListener {
                response.value = PurchaseResult(PurchaseCode.INVOICE_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INVOICE_DELETE_FAILURE)
            }

        return response
    }
}