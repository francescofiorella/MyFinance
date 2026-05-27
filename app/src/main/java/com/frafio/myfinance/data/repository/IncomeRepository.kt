package com.frafio.myfinance.data.repository

import com.frafio.myfinance.data.manager.IncomesManager
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.FinanceResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor(private val incomesManager: IncomesManager) {
    suspend fun updateIncomeList(): FinanceResult {
        return incomesManager.updateIncomeList()
    }

    suspend fun addIncome(income: Income): FinanceResult {
        return incomesManager.addIncome(income)
    }

    suspend fun editIncome(income: Income): FinanceResult {
        return incomesManager.editIncome(income)
    }

    suspend fun deleteIncome(income: Income): FinanceResult {
        return incomesManager.deleteIncome(income)
    }
}