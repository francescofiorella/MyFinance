package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.data.managers.InvoiceManager
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.InvoiceItem

class ReceiptRepository(private val invoiceManager: InvoiceManager) {

    fun getOptions(purchaseID: String): FirestoreRecyclerOptions<InvoiceItem> {
        val query = invoiceManager.getQuery(purchaseID)

        return FirestoreRecyclerOptions.Builder<InvoiceItem>().setQuery(
            query,
            InvoiceItem::class.java
        ).build()
    }

    fun addReceiptItem(invoiceItem: InvoiceItem, purchaseID: String): LiveData<PurchaseResult> {
        return invoiceManager.addItem(invoiceItem, purchaseID)
    }

    fun deleteReceiptItem(invoiceItem: InvoiceItem, purchaseID: String): LiveData<PurchaseResult> {
        return invoiceManager.deleteItem(invoiceItem, purchaseID)
    }
}