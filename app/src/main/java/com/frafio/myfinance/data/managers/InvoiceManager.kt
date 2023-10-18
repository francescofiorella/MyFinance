package com.frafio.myfinance.data.managers

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.DbReceipt
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.InvoiceItemStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.google.firebase.firestore.FirebaseFirestore

class InvoiceManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = InvoiceManager::class.java.simpleName
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun getInvoiceItems(purchaseID: String): LiveData<List<InvoiceItem>> {
        val response = MutableLiveData<List<InvoiceItem>>()
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchaseID).collection(DbReceipt.FIELDS.RECEIPT.value)
            .orderBy(DbReceipt.FIELDS.NAME.value)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                InvoiceItemStorage.setList(
                    queryDocumentSnapshots.map { document ->
                        document.toObject(InvoiceItem::class.java).also { it.id = document.id }
                    }.toMutableList()
                )
                response.value = InvoiceItemStorage.getList()
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = mutableListOf()
            }
        return response
    }

    fun addItem(
        invoiceItem: InvoiceItem,
        purchaseID: String
    ): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchaseID).collection(DbReceipt.FIELDS.RECEIPT.value)
            .add(invoiceItem).addOnSuccessListener { document ->
                invoiceItem.id = document.id
                InvoiceItemStorage.addToList(invoiceItem)
                response.value = PurchaseResult(PurchaseCode.INVOICE_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INVOICE_ADD_FAILURE)
            }

        return response
    }

    fun deleteItem(
        invoiceItem: InvoiceItem,
        purchaseID: String
    ): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchaseID).collection(DbReceipt.FIELDS.RECEIPT.value)
            .document(invoiceItem.id!!).delete().addOnSuccessListener {
                InvoiceItemStorage.removeFromList(invoiceItem)
                response.value = PurchaseResult(PurchaseCode.INVOICE_DELETE_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.INVOICE_DELETE_FAILURE)
            }

        return response
    }
}