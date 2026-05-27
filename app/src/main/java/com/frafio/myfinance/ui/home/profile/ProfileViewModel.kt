package com.frafio.myfinance.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.UserRepository
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiEvent {
    data class ShowSnackBar(
        val message: String,
        val actionText: String? = null,
        val actionFun: () -> Unit = {},
        val dismissFun: () -> Unit = {}
    ) : ProfileUiEvent()
    object LoadingStarted : ProfileUiEvent()
    object LoadingFinished : ProfileUiEvent()
    data class FullNameUpdated(val previousFullName: String) : ProfileUiEvent()
    object DynamicColorChanged : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val _user = MutableStateFlow(userRepository.getUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _scrollToTop = MutableSharedFlow<Unit>(replay = 0)
    val scrollToTop: SharedFlow<Unit> = _scrollToTop.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<ProfileUiEvent>()
    val uiEvents: SharedFlow<ProfileUiEvent> = _uiEvents.asSharedFlow()

    val googleSignIn: Boolean
        get() = _user.value?.provider == User.GOOGLE_PROVIDER

    fun uploadPropic() {
        viewModelScope.launch {
            _uiEvents.emit(ProfileUiEvent.LoadingStarted)
            val propicUri = ""
            val response = userRepository.updatePropic(propicUri)
            if (response.code == AuthCode.USER_PROPIC_UPDATED.code) {
                updateLocalUser()
            }
            _uiEvents.emit(ProfileUiEvent.ShowSnackBar(response.message))
            _uiEvents.emit(ProfileUiEvent.LoadingFinished)
        }
    }

    fun editFullName(fullName: String, notify: Boolean = true) {
        viewModelScope.launch {
            _uiEvents.emit(ProfileUiEvent.LoadingStarted)
            val previousFN = _user.value?.fullName ?: ""
            val response = userRepository.updateFullName(fullName)
            if (response.code == AuthCode.USER_FULL_NAME_UPDATED.code) {
                updateLocalUser()
                if (notify) {
                    _uiEvents.emit(ProfileUiEvent.FullNameUpdated(previousFN))
                }
            } else {
                if (notify) {
                    _uiEvents.emit(ProfileUiEvent.ShowSnackBar(response.message))
                }
            }
            _uiEvents.emit(ProfileUiEvent.LoadingFinished)
        }
    }

    fun updateLocalUser() {
        _user.value = userRepository.getUser()
    }

    fun scrollToTop() {
        viewModelScope.launch {
            _scrollToTop.emit(Unit)
        }
    }

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    val isDynamicColorAvailable: Boolean = DynamicColors.isDynamicColorAvailable()
    
    private val _isSwitchDynamicColorChecked = MutableStateFlow(getDynamicColorCheck())
    val isSwitchDynamicColorChecked: StateFlow<Boolean> = _isSwitchDynamicColorChecked.asStateFlow()

    fun setDynamicColor(active: Boolean) {
        expensesRepository.setDynamicColorActive(active)
        _isSwitchDynamicColorChecked.value = active
        viewModelScope.launch {
            _uiEvents.emit(ProfileUiEvent.DynamicColorChanged)
        }
    }

    private fun getDynamicColorCheck(): Boolean {
        return expensesRepository.getDynamicColorActive()
    }
}
