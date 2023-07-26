package com.frafio.myfinance.ui.home.payments.invoice

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.PurchaseResult

interface InvoiceListener {
    fun onLoadStarted()

    fun onLoadSuccess(response: LiveData<PurchaseResult>)

    fun onLoadFailure(result: PurchaseResult)
}