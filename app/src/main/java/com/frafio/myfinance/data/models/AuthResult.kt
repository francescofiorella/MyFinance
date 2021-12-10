package com.frafio.myfinance.data.models

import com.frafio.myfinance.data.enums.auth.AuthCodeIT

data class AuthResult(private val result: AuthCodeIT, private val customMessage: String? = null) {
    val code: Int = result.code
    val message: String = customMessage ?: result.message
}