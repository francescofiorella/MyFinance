package com.frafio.myfinance.ui.home.list.invoice

import android.view.View
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.InvoiceItem
import com.frafio.myfinance.data.repositories.ReceiptRepository

class InvoiceViewModel(
    private val repository: ReceiptRepository,
) : ViewModel() {
    var purchaseID: String? = null
    var purchaseName: String? = null
    var purchasePrice: String? = null

    var receiptName: String? = null
    var receiptPrice: String? = null

    var listener: InvoiceListener? = null

    fun getOptions(): FirestoreRecyclerOptions<InvoiceItem> {
        return repository.getOptions(purchaseID!!)
    }

    fun onAddButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        listener?.onLoadStarted()

        // check info
        if (receiptName.isNullOrEmpty()) {
            listener?.onLoadFailure(PurchaseResult(PurchaseCode.EMPTY_NAME))
            return
        }

        if (receiptPrice.isNullOrEmpty()) {
            listener?.onLoadFailure(PurchaseResult(PurchaseCode.EMPTY_PRICE))
            return
        }

        val price = receiptPrice!!.toDouble()
        val item = InvoiceItem(receiptName, price)

        val response = repository.addReceiptItem(item, purchaseID!!)
        listener?.onLoadSuccess(response)
    }

    fun onDeleteClick(invoiceItem: InvoiceItem) {
        val response = repository.deleteReceiptItem(invoiceItem, purchaseID!!)
        listener?.onLoadSuccess(response)
    }
}