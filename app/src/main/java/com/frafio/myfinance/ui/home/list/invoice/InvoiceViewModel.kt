package com.frafio.myfinance.ui.home.list.invoice

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.repositories.InvoiceRepository

class InvoiceViewModel(application: Application) : AndroidViewModel(application) {
    private val invoiceRepository = InvoiceRepository(
        (application as MyFinanceApplication).invoiceManager
    )

    var purchaseID: String? = null
    var purchaseName: String? = null
    var purchasePrice: String? = null

    var invoiceName: String? = null
    var invoicePrice: String? = null

    var listener: InvoiceListener? = null

    fun getInvoiceItems(): LiveData<List<InvoiceItem>> {
        return invoiceRepository.getInvoiceItemsList(purchaseID ?: "")
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

        val response = invoiceRepository.addInvoiceItem(item, purchaseID!!)
        listener?.onLoadSuccess(response)
    }

    fun onDeleteClick(invoiceItem: InvoiceItem) {
        val response = invoiceRepository.deleteInvoiceItem(invoiceItem, purchaseID!!)
        listener?.onLoadSuccess(response)
    }
}