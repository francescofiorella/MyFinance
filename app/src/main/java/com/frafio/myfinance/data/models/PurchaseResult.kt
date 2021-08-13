package com.frafio.myfinance.data.models

import com.frafio.myfinance.data.enums.db.AddCode

data class PurchaseResult(private val result: AddCode, private val customMessage: String? = null) {
    val code: Int = result.code
    val message: String = customMessage ?: result.message
}