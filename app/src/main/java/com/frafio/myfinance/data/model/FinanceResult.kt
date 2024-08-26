package com.frafio.myfinance.data.model

import com.frafio.myfinance.data.enums.db.FinanceCode

data class FinanceResult(private val result: FinanceCode, private val customMessage: String? = null) {
    val code: Int = result.code
    val message: String = customMessage ?: result.message
}