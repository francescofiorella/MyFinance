package com.frafio.myfinance.ui.home.list.receipt

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.PurchaseResult

interface ReceiptListener {
    fun onLoadSuccess(response: LiveData<PurchaseResult>)

    fun onLoadFailure(result: PurchaseResult)
}