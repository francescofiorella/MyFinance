package com.frafio.myfinance.ui.home.list.receipt

import android.view.View
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.models.ReceiptItem
import com.frafio.myfinance.data.repositories.ReceiptRepository

class ReceiptViewModel(
    private val repository: ReceiptRepository,
) : ViewModel() {
    var purchaseID: String? = null
    var purchaseName: String? = null
    var purchasePrice: String? = null

    var receiptName: String? = null
    var receiptPrice: String? = null

    var listener: ReceiptListener? = null

    fun getOptions(): FirestoreRecyclerOptions<ReceiptItem> {
        return repository.getOptions(purchaseID!!)
    }

    fun onAddButtonClick(view: View) {
        // controlla la info aggiunte
        if (receiptName.isNullOrEmpty()) {
            listener?.onLoadFailure(PurchaseResult(PurchaseCode.EMPTY_NAME))
            return
        }

        if (receiptPrice.isNullOrEmpty()) {
            listener?.onLoadFailure(PurchaseResult(PurchaseCode.EMPTY_PRICE))
            return
        }

        val price = receiptPrice!!.toDouble()
        val item = ReceiptItem(receiptName, price)

        val response = repository.addReceiptItem(item, purchaseID!!)
        listener?.onLoadSuccess(response)
    }

    fun onDeleteClick(receiptItem: ReceiptItem) {
        val response = repository.deleteReceiptItem(receiptItem, purchaseID!!)
        listener?.onLoadSuccess(response)
    }
}