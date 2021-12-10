package com.frafio.myfinance.data.models

import com.frafio.myfinance.data.enums.db.PurchaseCodeIT

data class PurchaseResult(private val result: PurchaseCodeIT, private val customMessage: String? = null) {
    val code: Int = result.code
    val message: String = customMessage ?: result.message
}