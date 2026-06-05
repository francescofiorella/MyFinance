package com.frafio.myfinance.features.auth

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiEvent {
    object Success : AuthUiEvent()

    data class Error(val message: String) : AuthUiEvent()
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

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onFullNameChange(fullName: String) {
        _uiState.update { it.copy(fullName = fullName, fullNameError = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
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
            if (!validateLogin()) return@launch
            loadingRepository.startLoading()

            val result = userRepository.userLogin(_uiState.value.email, _uiState.value.password)
            loadingRepository.stopLoading()

            if (result.code == AuthCode.LOGIN_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.Success)
            } else {
                _uiEvents.emit(AuthUiEvent.Error(result.message))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            loadingRepository.startLoading()
            
            val result = userRepository.resetPassword(email)
            
            loadingRepository.stopLoading()

            if (result.code == AuthCode.EMAIL_SENT.code) {
                _uiEvents.emit(AuthUiEvent.Success)
            } else {
                _uiEvents.emit(AuthUiEvent.Error(result.message))
            }
        }
    }

    fun onGoogleRequest(credential: Credential) {
        viewModelScope.launch {
            val result = userRepository.userLogin(credential)
            
            if (result.code == AuthCode.LOGIN_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.Success)
            } else {
                _uiEvents.emit(AuthUiEvent.Error(result.message))
            }

            loadingRepository.stopLoading()
        }
    }

    fun onSignupButtonClick() {
        viewModelScope.launch {
            if (!validateSignup()) return@launch
            loadingRepository.startLoading()

            val result = userRepository.userSignup(
                _uiState.value.fullName,
                _uiState.value.email,
                _uiState.value.password
            )
            
            loadingRepository.stopLoading()

            if (result.code == AuthCode.SIGNUP_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.Success)
            } else {
                _uiEvents.emit(AuthUiEvent.Error(result.message))
            }
        }
    }

    private fun validateLogin(): Boolean {
        var hasError = false
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_EMAIL)
            _uiState.update { it.copy(emailError = error.message) }
            hasError = true
        }

        if (password.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_PASSWORD)
            _uiState.update { it.copy(passwordError = error.message) }
            hasError = true
        } else if (password.length < 8) {
            val error = AuthResult(AuthCode.SHORT_PASSWORD)
            _uiState.update { it.copy(passwordError = error.message) }
            hasError = true
        }

        return !hasError
    }

    private fun validateSignup(): Boolean {
        var hasError = false
        val state = _uiState.value

        if (state.fullName.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_NAME)
            _uiState.update { it.copy(fullNameError = error.message) }
            hasError = true
        }

        if (state.email.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_EMAIL)
            _uiState.update { it.copy(emailError = error.message) }
            hasError = true
        }

        if (state.password.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_PASSWORD)
            _uiState.update { it.copy(passwordError = error.message) }
            hasError = true
        } else if (state.password.length < 8) {
            val error = AuthResult(AuthCode.SHORT_PASSWORD)
            _uiState.update { it.copy(passwordError = error.message) }
            hasError = true
        }

        if (state.confirmPassword.isEmpty()) {
            val error = AuthResult(AuthCode.EMPTY_CONFIRM_PASSWORD)
            _uiState.update { it.copy(confirmPasswordError = error.message) }
            hasError = true
        } else if (state.password.isNotEmpty() && state.confirmPassword != state.password) {
            val error = AuthResult(AuthCode.PASSWORD_NOT_MATCH)
            _uiState.update { it.copy(confirmPasswordError = error.message) }
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
