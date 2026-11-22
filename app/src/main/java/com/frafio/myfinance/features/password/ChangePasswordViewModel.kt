package com.frafio.myfinance.features.password

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChangePasswordUiEvent {
    data class ShowSnackBar(
        @StringRes val messageRes: Int,
        val actionText: String? = null,
        val actionFun: () -> Unit = {},
        val dismissFun: () -> Unit = {}
    ) : ChangePasswordUiEvent()

    data class InvalidCurrentPassword(@StringRes val messageRes: Int) : ChangePasswordUiEvent()

    data class Success(@StringRes val messageRes: Int) : ChangePasswordUiEvent()
}

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loadingRepository: LoadingRepository
) : ViewModel() {

    val isLoading: StateFlow<Boolean> = loadingRepository.isLoading

    val user: StateFlow<User?> = userRepository.userData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), userRepository.getCurrentUser())

    private val _uiEvents = MutableSharedFlow<ChangePasswordUiEvent>()
    val uiEvents: SharedFlow<ChangePasswordUiEvent> = _uiEvents.asSharedFlow()

    fun changePassword(newPassword: String, currentPassword: String? = null) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val response = userRepository.changePassword(newPassword, currentPassword)
                when (response.code) {
                    AuthCode.PASSWORD_UPDATED.code -> {
                        _uiEvents.emit(ChangePasswordUiEvent.Success(response.messageRes))
                    }
                    AuthCode.WRONG_OLD_PASSWORD.code -> {
                        _uiEvents.emit(ChangePasswordUiEvent.InvalidCurrentPassword(response.messageRes))
                    }
                    else -> {
                        _uiEvents.emit(ChangePasswordUiEvent.ShowSnackBar(response.messageRes))
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun resetPassword() {
        val email = user.value?.email ?: return
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val response = userRepository.resetPassword(email)
                _uiEvents.emit(ChangePasswordUiEvent.ShowSnackBar(response.messageRes))
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }
}
