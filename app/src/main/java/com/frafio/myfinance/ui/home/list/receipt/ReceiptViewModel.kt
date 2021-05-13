package com.frafio.myfinance.ui.home.list.receipt

import android.view.View
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.data.models.ReceiptItem
import com.frafio.myfinance.data.repositories.ReceiptRepository

class ReceiptViewModel(
    private val repository: ReceiptRepository,
) : ViewModel() {
    private var purchaseID: String? = null
    var purchaseName: String? = null
    var purchasePrice: String? = null

    var receiptName: String? = null
    var receiptPrice: String? = null

    var listener: ReceiptListener? = null

    fun setPurchaseID(id: String) {
        purchaseID = id
    }

    fun setOptions(): FirestoreRecyclerOptions<ReceiptItem> {
        return repository.setOptions(purchaseID!!)
    }

    fun onAddButtonClick(view: View) {
        // controlla la info aggiunte
        if (receiptName.isNullOrEmpty()) {
            listener?.onLoadFailure(1)
            return
        }

        if (receiptPrice.isNullOrEmpty()) {
            listener?.onLoadFailure(2)
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