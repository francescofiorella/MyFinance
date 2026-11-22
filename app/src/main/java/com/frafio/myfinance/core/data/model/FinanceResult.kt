package com.frafio.myfinance.core.data.model

import androidx.annotation.StringRes
import com.frafio.myfinance.core.data.enums.db.FinanceCode

data class FinanceResult(private val result: FinanceCode) {
    val code: Int = result.code
    @StringRes val messageRes: Int = result.messageRes
}

data class DeleteLabelResult(
    val financeResult: FinanceResult,
    val affectedExpenses: List<Expense> = emptyList()
)
