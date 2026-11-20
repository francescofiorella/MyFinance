package com.frafio.myfinance.features.auth

/**
 * UI state for the authentication screen. The field texts live in the ViewModel's `TextFieldState`s.
 */
data class AuthUiState(
    val isSigningUp: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val fullNameError: String? = null,
    val confirmPasswordError: String? = null,
    val showResetPasswordSheet: Boolean = false
)
