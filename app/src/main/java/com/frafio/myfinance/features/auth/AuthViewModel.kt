package com.frafio.myfinance.features.auth

import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.UserRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiEvent {
    object Loading : AuthUiEvent()
    data class Success(val authResult: AuthResult) : AuthUiEvent()
    data class Error(val authResult: AuthResult) : AuthUiEvent()
    object NavigateToHome : AuthUiEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferencesData?> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    var email: String? = null
    var password: String? = null

    var fullName: String? = null
    var passwordConfirm: String? = null

    var isSigningUp: Boolean = false

    var credentialManager: CredentialManager? = null

    private val _uiEvents = MutableSharedFlow<AuthUiEvent>()
    val uiEvents: SharedFlow<AuthUiEvent> = _uiEvents.asSharedFlow()

    fun onLoginButtonClick() {
        viewModelScope.launch {
            _uiEvents.emit(AuthUiEvent.Loading)

            var hasError = false
            if (email.isNullOrEmpty()) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.EMPTY_EMAIL)))
                hasError = true
            }

            if (password.isNullOrEmpty()) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.EMPTY_PASSWORD)))
                hasError = true
            }

            if (!password.isNullOrEmpty() && password!!.length < 8) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.SHORT_PASSWORD)))
                hasError = true
            }

            if (hasError) return@launch

            val loginResult = userRepository.userLogin(email!!, password!!)
            if (loginResult.code == AuthCode.LOGIN_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.NavigateToHome)
            } else {
                _uiEvents.emit(AuthUiEvent.Success(loginResult))
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiEvents.emit(AuthUiEvent.Loading)
            val resetResult = userRepository.resetPassword(email)
            _uiEvents.emit(AuthUiEvent.Success(resetResult))
        }
    }

    fun onGoogleRequest(credential: Credential) {
        viewModelScope.launch {
            val googleResult = userRepository.userLogin(credential)
            if (googleResult.code == AuthCode.LOGIN_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.NavigateToHome)
            } else {
                _uiEvents.emit(AuthUiEvent.Success(googleResult))
            }
        }
    }

    fun onSignupButtonClick() {
        viewModelScope.launch {
            _uiEvents.emit(AuthUiEvent.Loading)

            // check info
            var hasError = false
            if (fullName.isNullOrEmpty()) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.EMPTY_NAME)))
                hasError = true
            }

            if (email.isNullOrEmpty()) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.EMPTY_EMAIL)))
                hasError = true
            }

            if (password.isNullOrEmpty()) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.EMPTY_PASSWORD)))
                hasError = true
            } else if (password!!.length < 8) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.SHORT_PASSWORD)))
                hasError = true
            }

            if (passwordConfirm.isNullOrEmpty()) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.EMPTY_CONFIRM_PASSWORD)))
                hasError = true
            } else if (!password.isNullOrEmpty() && passwordConfirm != password) {
                _uiEvents.emit(AuthUiEvent.Error(AuthResult(AuthCode.PASSWORD_NOT_MATCH)))
                hasError = true
            }

            if (hasError) return@launch

            val signupResult = userRepository.userSignup(fullName!!, email!!, password!!)
            if (signupResult.code == AuthCode.SIGNUP_SUCCESS.code) {
                _uiEvents.emit(AuthUiEvent.NavigateToHome)
            } else {
                _uiEvents.emit(AuthUiEvent.Success(signupResult))
            }
        }
    }
}
