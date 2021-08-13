package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.data.managers.ReceiptManager
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.ReceiptItem

class ReceiptRepository(private val receiptManager: ReceiptManager) {

    fun getOptions(purchaseID: String): FirestoreRecyclerOptions<ReceiptItem> {
        val query = receiptManager.getQuery(purchaseID)

        return FirestoreRecyclerOptions.Builder<ReceiptItem>().setQuery(
            query,
            ReceiptItem::class.java
        ).build()
    }

    fun addReceiptItem(receiptItem: ReceiptItem, purchaseID: String): LiveData<PurchaseResult> {
        return receiptManager.addItem(receiptItem, purchaseID)
    }

    fun deleteReceiptItem(receiptItem: ReceiptItem, purchaseID: String): LiveData<PurchaseResult> {
        return receiptManager.deleteItem(receiptItem, purchaseID)
    }
}