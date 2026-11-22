package com.frafio.myfinance.core.data.model

import androidx.annotation.StringRes
import com.frafio.myfinance.core.data.enums.auth.AuthCode

data class AuthResult(private val result: AuthCode) {
    val code: Int = result.code
    @StringRes val messageRes: Int = result.messageRes
}
