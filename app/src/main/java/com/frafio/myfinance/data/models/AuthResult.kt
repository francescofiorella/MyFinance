package com.frafio.myfinance.data.models

import com.frafio.myfinance.data.enums.auth.AUTH_RESULT

data class AuthResult(private val result: AUTH_RESULT, private val customMessage: String? = null) {
    val code: Int = result.code
    val message: String = customMessage ?: result.message
}