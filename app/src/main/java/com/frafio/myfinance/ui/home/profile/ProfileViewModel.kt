package com.frafio.myfinance.ui.home.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.UserRepository
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository((application as MyFinanceApplication).authManager)
    private val expensesRepository =
        ExpensesRepository((application as MyFinanceApplication).expensesManager)

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
    }

    private fun getDynamicColorCheck(): Boolean {
        return expensesRepository.getDynamicColorActive()
    }
}
