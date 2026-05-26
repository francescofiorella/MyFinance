package com.frafio.myfinance.ui.home.budget

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.FinanceResult

interface BudgetListener {
    fun onStarted(notify: Boolean = true)
    fun onCompleted(response: LiveData<FinanceResult>, previousBudget: Double?, notify: Boolean = true)

    fun onDeleteCompleted(response: LiveData<FinanceResult>, income: Income)
}