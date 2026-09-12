package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.model.Income
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope

interface IncomeRepository {

    suspend fun addIncome(income: Income): FinanceResult

    suspend fun editIncome(income: Income): FinanceResult

    suspend fun deleteIncome(income: Income): FinanceResult

    fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>? = null
    )

    fun stopSnapshotListener()
}
