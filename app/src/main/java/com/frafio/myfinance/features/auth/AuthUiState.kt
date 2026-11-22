package com.frafio.myfinance.features.auth

import androidx.annotation.StringRes

/**
 * UI state for the authentication screen. The field texts live in the ViewModel's `TextFieldState`s;
 * the errors are string resources, resolved by the screen.
 */
data class AuthUiState(
    val isSigningUp: Boolean = false,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val fullNameError: Int? = null,
    @StringRes val confirmPasswordError: Int? = null,
    val showResetPasswordSheet: Boolean = false
)
