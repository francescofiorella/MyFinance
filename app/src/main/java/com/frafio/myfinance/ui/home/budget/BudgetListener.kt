package com.frafio.myfinance.ui.home.budget

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult

interface BudgetListener {
    fun onCompleted(response: LiveData<PurchaseResult>, previousBudget: Double?)

    fun onDeleteCompleted(response: LiveData<PurchaseResult>, income: Purchase)
}