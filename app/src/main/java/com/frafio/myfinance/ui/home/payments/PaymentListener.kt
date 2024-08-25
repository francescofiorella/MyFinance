package com.frafio.myfinance.ui.home.payments

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.Purchase
import com.frafio.myfinance.data.model.PurchaseResult

interface PaymentListener {
    fun onCompleted(response: LiveData<PurchaseResult>)

    fun onDeleteCompleted(response: LiveData<PurchaseResult>, purchase: Purchase)
}