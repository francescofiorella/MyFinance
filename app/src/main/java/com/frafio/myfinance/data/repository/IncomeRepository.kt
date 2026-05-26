package com.frafio.myfinance.data.repository

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.manager.IncomesManager
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.FinanceResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor(private val incomesManager: IncomesManager) {
    fun updateIncomeList(): LiveData<FinanceResult> {
        return incomesManager.updateIncomeList()
    }

    fun addIncome(income: Income): LiveData<FinanceResult> {
        return incomesManager.addIncome(income)
    }

    fun editIncome(income: Income): LiveData<FinanceResult> {
        return incomesManager.editIncome(income)
    }

    fun deleteIncome(income: Income): LiveData<FinanceResult> {
        return incomesManager.deleteIncome(income)
    }
}