package com.frafio.myfinance.features.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.credentials.Credential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiEvent {
    object Success : AuthUiEvent()
    data class Message(@StringRes val messageRes: Int) : AuthUiEvent()
    data class Error(@StringRes val messageRes: Int) : AuthUiEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loadingRepository: LoadingRepository
) : ViewModel() {

    val isLoading: StateFlow<Boolean> = loadingRepository.isLoading

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<AuthUiEvent>()
    val uiEvents: SharedFlow<AuthUiEvent> = _uiEvents.asSharedFlow()

    val emailState = TextFieldState()
    val passwordState = TextFieldState()
    val fullNameState = TextFieldState()
    val confirmPasswordState = TextFieldState()

    init {
        // Trimmed fields watch the trimmed text, so the trim on submit keeps the error validation just set.
        clearErrorOnEdit({ emailState.text.trim() }) { it.copy(emailError = null) }
        clearErrorOnEdit({ passwordState.text }) { it.copy(passwordError = null) }
        clearErrorOnEdit({ fullNameState.text.trim() }) { it.copy(fullNameError = null) }
        clearErrorOnEdit({ confirmPasswordState.text }) { it.copy(confirmPasswordError = null) }
    }

    private fun clearErrorOnEdit(text: () -> CharSequence, clear: (AuthUiState) -> AuthUiState) {
        snapshotFlow { text().toString() }
            .drop(1)
            .onEach { _uiState.update(clear) }
            .launchIn(viewModelScope)
    }

    fun onToggleAuthMode() {
        _uiState.update {
            it.copy(
                isSigningUp = !it.isSigningUp,
                emailError = null,
                passwordError = null,
                fullNameError = null,
                confirmPasswordError = null
            )
        }
    }

    fun setShowResetPasswordSheet(show: Boolean) {
        _uiState.update { it.copy(showResetPasswordSheet = show) }
    }

    fun onLoginButtonClick() {
        viewModelScope.launch {
            emailState.trim()
            if (!validateLogin()) return@launch
            loadingRepository.startLoading()

            val result = userRepository.userLogin(emailState.text.toString(), passwordState.text.toString())
            loadingRepository.stopLoading()

            if (result.code == AuthCode.LOGIN_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.Success)
            } else {
                _uiEvents.emit(AuthUiEvent.Error(result.messageRes))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            loadingRepository.startLoading()
            
            val result = userRepository.resetPassword(email.trim())
            
            loadingRepository.stopLoading()

            if (result.code == AuthCode.EMAIL_SENT.code) {
                _uiEvents.emit(AuthUiEvent.Message(result.messageRes))
            } else {
                _uiEvents.emit(AuthUiEvent.Error(result.messageRes))
            }
        }
    }

    fun onGoogleRequest(credential: Credential) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val result = userRepository.userLogin(credential)

                if (result.code == AuthCode.LOGIN_SUCCESS.code) {
                    _uiEvents.emit(AuthUiEvent.Success)
                } else {
                    _uiEvents.emit(AuthUiEvent.Error(result.messageRes))
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun onSignupButtonClick() {
        viewModelScope.launch {
            fullNameState.trim()
            emailState.trim()
            if (!validateSignup()) return@launch
            loadingRepository.startLoading()

            val result = userRepository.userSignup(
                fullNameState.text.toString(),
                emailState.text.toString(),
                passwordState.text.toString()
            )
            
            loadingRepository.stopLoading()

            if (result.code == AuthCode.SIGNUP_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.Success)
            } else {
                _uiEvents.emit(AuthUiEvent.Error(result.messageRes))
            }
        }
    }

    private fun TextFieldState.trim() {
        val trimmed = text.trim()
        if (trimmed.length != text.length) setTextAndPlaceCursorAtEnd(trimmed.toString())
    }

    private fun validateLogin(): Boolean {
        var hasError = false
        val email = emailState.text
        val password = passwordState.text

        if (email.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_EMAIL)
            _uiState.update { it.copy(emailError = error.messageRes) }
            hasError = true
        }

        if (password.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_PASSWORD)
            _uiState.update { it.copy(passwordError = error.messageRes) }
            hasError = true
        } else if (password.length < 8) {
            val error = AuthResult(AuthCode.SHORT_PASSWORD)
            _uiState.update { it.copy(passwordError = error.messageRes) }
            hasError = true
        }

        return !hasError
    }

    private fun validateSignup(): Boolean {
        var hasError = false
        val fullName = fullNameState.text
        val email = emailState.text
        val password = passwordState.text.toString()
        val confirmPassword = confirmPasswordState.text.toString()

        if (fullName.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_NAME)
            _uiState.update { it.copy(fullNameError = error.messageRes) }
            hasError = true
        }

        if (email.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_EMAIL)
            _uiState.update { it.copy(emailError = error.messageRes) }
            hasError = true
        }

        if (password.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_PASSWORD)
            _uiState.update { it.copy(passwordError = error.messageRes) }
            hasError = true
        } else if (password.length < 8) {
            val error = AuthResult(AuthCode.SHORT_PASSWORD)
            _uiState.update { it.copy(passwordError = error.messageRes) }
            hasError = true
        }

        if (confirmPassword.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_CONFIRM_PASSWORD)
            _uiState.update { it.copy(confirmPasswordError = error.messageRes) }
            hasError = true
        } else if (password.isNotEmpty() && confirmPassword != password) {
            val error = AuthResult(AuthCode.PASSWORD_NOT_MATCH)
            _uiState.update { it.copy(confirmPasswordError = error.messageRes) }
            hasError = true
        }

        return !hasError
    }

    fun startLoading() {
        loadingRepository.startLoading()
    }

    fun stopLoading() {
        loadingRepository.stopLoading()
    }
}
