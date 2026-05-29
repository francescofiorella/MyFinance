package com.frafio.myfinance.ui.home.profile

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.LoadingRepository
import com.frafio.myfinance.data.repository.UserRepository
import com.frafio.myfinance.data.repository.UserPreferencesData
import com.frafio.myfinance.data.repository.UserPreferencesRepository
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiEvent {
    data class ShowSnackBar(
        val message: String,
        val actionText: String? = null,
        val actionFun: () -> Unit = {},
        val dismissFun: () -> Unit = {}
    ) : ProfileUiEvent()
    data class FullNameUpdated(val previousFullName: String) : ProfileUiEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val expensesRepository: ExpensesRepository,
    userPreferencesRepository: UserPreferencesRepository,
    private val loadingRepository: LoadingRepository,
    profileImageStorage: com.frafio.myfinance.data.storage.ProfileImageStorage
) : ViewModel() {

    val user: StateFlow<User?> = userRepository.userData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), userRepository.getCurrentUser())

    val profilePicture: StateFlow<Bitmap?> = userRepository.profilePicture
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), profileImageStorage.loadBitmapSync())

    private val _scrollToTop = MutableSharedFlow<Unit>(replay = 0)
    val scrollToTop: SharedFlow<Unit> = _scrollToTop.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<ProfileUiEvent>()
    val uiEvents: SharedFlow<ProfileUiEvent> = _uiEvents.asSharedFlow()

    val userPreferences: StateFlow<UserPreferencesData?> = userPreferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSwitchDynamicColorChecked: StateFlow<Boolean> = userPreferences
        .map { it?.dynamicColor ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun uploadPropic() {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val propicUri = ""
                val response = userRepository.updatePropic(propicUri)
                _uiEvents.emit(ProfileUiEvent.ShowSnackBar(response.message))
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun editFullName(fullName: String, notify: Boolean = true) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val previousFN = user.value?.fullName ?: ""
                val response = userRepository.updateFullName(fullName)
                if (response.code == AuthCode.USER_FULL_NAME_UPDATED.code) {
                    if (notify) {
                        _uiEvents.emit(ProfileUiEvent.FullNameUpdated(previousFN))
                    }
                } else {
                    if (notify) {
                        _uiEvents.emit(ProfileUiEvent.ShowSnackBar(response.message))
                    }
                }
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun scrollToTop() {
        viewModelScope.launch {
            _scrollToTop.emit(Unit)
        }
    }

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    val isDynamicColorAvailable: Boolean = DynamicColors.isDynamicColorAvailable()

    fun setDynamicColor(active: Boolean) {
        viewModelScope.launch {
            expensesRepository.setDynamicColorActive(active)
        }
    }
}
