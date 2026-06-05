package com.frafio.myfinance.features.auth

/**
 * UI state for the authentication screen.
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val confirmPassword: String = "",
    val isSigningUp: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val fullNameError: String? = null,
    val confirmPasswordError: String? = null,
    val showResetPasswordSheet: Boolean = false
)
