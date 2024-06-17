package com.frafio.myfinance.ui.home.payments

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult

interface PaymentListener {
    fun onUpdateTypeComplete(response: LiveData<PurchaseResult>)

    fun onDeleteComplete(response: LiveData<PurchaseResult>, purchase: Purchase)

    fun onDeleteCancelComplete(response: LiveData<PurchaseResult>)
}