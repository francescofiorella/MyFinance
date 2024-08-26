package com.frafio.myfinance.ui.home.budget

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.FinanceResult

interface BudgetListener {
    fun onCompleted(response: LiveData<FinanceResult>, previousBudget: Double?)

    fun onDeleteCompleted(response: LiveData<FinanceResult>, income: Income)
}