package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.manager.IncomesSyncManager
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.FinanceResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepositoryImpl @Inject constructor(private val incomesManager: IncomesSyncManager) : IncomeRepository {
    override suspend fun addIncome(income: Income): FinanceResult {
        return incomesManager.add(income)
    }

    override suspend fun editIncome(income: Income): FinanceResult {
        return incomesManager.edit(income)
    }

    override suspend fun deleteIncome(income: Income): FinanceResult {
        return incomesManager.delete(income)
    }

    override fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>?
    ) {
        incomesManager.startSnapshotListener(scope, onInitialSync)
    }

    override fun stopSnapshotListener() {
        incomesManager.stopSnapshotListener()
    }
}
