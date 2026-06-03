package com.frafio.myfinance.core.data.model

import com.frafio.myfinance.core.data.enums.db.FinanceCode

data class FinanceResult(private val result: FinanceCode, private val customMessage: String? = null) {
    val code: Int = result.code
    val message: String = customMessage ?: result.message
}