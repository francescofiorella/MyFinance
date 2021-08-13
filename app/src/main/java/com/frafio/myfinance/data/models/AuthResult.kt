package com.frafio.myfinance.data.models

import com.frafio.myfinance.data.enums.auth.AuthCode

data class AuthResult(private val result: AuthCode, private val customMessage: String? = null) {
    val code: Int = result.code
    val message: String = customMessage ?: result.message
}