package com.frafio.myfinance.ui.home.list.invoice

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.data.repositories.InvoiceRepository

class InvoiceViewModel(
    private val repository: InvoiceRepository,
) : ViewModel() {
    var purchaseID: String? = null
    var purchaseName: String? = null
    var purchasePrice: String? = null

    var invoiceName: String? = null
    var invoicePrice: String? = null

    var listener: InvoiceListener? = null

    fun getInvoiceItems(): LiveData<List<InvoiceItem>> {
        return repository.getInvoiceItemsList(purchaseID ?: "")
    }

    fun onAddButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        listener?.onLoadStarted()

        // check info
        if (invoiceName.isNullOrEmpty()) {
            listener?.onLoadFailure(PurchaseResult(PurchaseCode.EMPTY_NAME))
            return
        }

        if (invoicePrice.isNullOrEmpty()) {
            listener?.onLoadFailure(PurchaseResult(PurchaseCode.EMPTY_PRICE))
            return
        }

        val price = invoicePrice!!.toDouble()
        val item = InvoiceItem(invoiceName, price)

        val response = repository.addInvoiceItem(item, purchaseID!!)
        listener?.onLoadSuccess(response)
    }

    fun onDeleteClick(invoiceItem: InvoiceItem) {
        val response = repository.deleteInvoiceItem(invoiceItem, purchaseID!!)
        listener?.onLoadSuccess(response)
    }
}