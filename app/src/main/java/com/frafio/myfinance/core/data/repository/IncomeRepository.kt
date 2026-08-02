package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.manager.IncomesSyncManager
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.FinanceResult
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor(private val incomesManager: IncomesSyncManager) {
    suspend fun addIncome(income: Income): FinanceResult {
        return incomesManager.add(income)
    }

    suspend fun editIncome(income: Income): FinanceResult {
        return incomesManager.edit(income)
    }

    suspend fun deleteIncome(income: Income): FinanceResult {
        return incomesManager.delete(income)
    }

    fun startSnapshotListener(scope: CoroutineScope) {
        incomesManager.startSnapshotListener(scope)
    }

    fun stopSnapshotListener() {
        incomesManager.stopSnapshotListener()
    }
}
