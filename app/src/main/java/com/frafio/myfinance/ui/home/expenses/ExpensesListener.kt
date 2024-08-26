package com.frafio.myfinance.ui.home.expenses

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult

interface ExpensesListener {
    fun onCompleted(response: LiveData<FinanceResult>)

    fun onDeleteCompleted(response: LiveData<FinanceResult>, expense: Expense)
}