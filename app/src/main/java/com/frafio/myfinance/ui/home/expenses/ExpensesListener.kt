package com.frafio.myfinance.ui.home.expenses

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult

interface ExpensesListener {
    fun onStarted(notify: Boolean = true)
    fun onCompleted(response: LiveData<FinanceResult>, notify: Boolean = true)

    fun onDeleteCompleted(response: LiveData<FinanceResult>, expense: Expense)
    fun onDeleteCompleted(response: LiveData<FinanceResult>)
}