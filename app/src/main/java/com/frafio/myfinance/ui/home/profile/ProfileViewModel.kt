package com.frafio.myfinance.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.BuildConfig
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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private val _user = MutableStateFlow(userRepository.getUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _scrollToTop = MutableSharedFlow<Unit>(replay = 0)
    val scrollToTop: SharedFlow<Unit> = _scrollToTop.asSharedFlow()

    val googleSignIn: Boolean
        get() = _user.value?.provider == User.GOOGLE_PROVIDER

    var listener: ProfileListener? = null

    fun uploadPropic() {
        listener?.onStarted()
        val propicUri = ""
        val response = userRepository.updateProfile(null, propicUri)
        listener?.onProfileUpdateComplete(response)
    }

    fun editFullName(fullName: String) {
        listener?.onStarted()
        val response = userRepository.updateProfile(fullName, null)
        listener?.onProfileUpdateComplete(response)
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
        listener?.onDynamicColorChanged()
    }

    private fun getDynamicColorCheck(): Boolean {
        return expensesRepository.getDynamicColorActive()
    }
}
