package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.managers.InvoiceManager
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.InvoiceItem

class InvoiceRepository(private val invoiceManager: InvoiceManager) {

    fun getInvoiceItemsList(id: String): LiveData<List<InvoiceItem>> {
        return invoiceManager.getInvoiceItems(id)
    }

    fun addInvoiceItem(invoiceItem: InvoiceItem, purchaseID: String): LiveData<PurchaseResult> {
        return invoiceManager.addItem(invoiceItem, purchaseID)
    }

    fun deleteInvoiceItem(invoiceItem: InvoiceItem, purchaseID: String): LiveData<PurchaseResult> {
        return invoiceManager.deleteItem(invoiceItem, purchaseID)
    }
}