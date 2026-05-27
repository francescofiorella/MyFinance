package com.frafio.myfinance.ui.home

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.UserRepository
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeLogicEvent {
    object UserLocalDataLoaded : HomeLogicEvent()
    object UserNotLogged : HomeLogicEvent()
    object LogoutSuccess : HomeLogicEvent()
}

sealed class HomeUiEvent {
    data class ShowSnackBar(
        val message: String,
        val actionText: String? = null,
        val actionFun: () -> Unit = {},
        val dismissFun: () -> Unit = {}
    ) : HomeUiEvent()
    object LoginSuccess : HomeUiEvent()
    object LoadingStarted : HomeUiEvent()
    object LoadingFinished : HomeUiEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val expensesRepository: ExpensesRepository,
    private val incomeRepository: IncomeRepository
) : ViewModel() {

    private val _navEvents = Channel<MyFinanceNavKey>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    private val _uiEvents = MutableSharedFlow<HomeUiEvent>()
    val uiEvents: SharedFlow<HomeUiEvent> = _uiEvents.asSharedFlow()

    private val _logicEvents = MutableSharedFlow<HomeLogicEvent>()
    val logicEvents: SharedFlow<HomeLogicEvent> = _logicEvents.asSharedFlow()

    fun navigateTo(key: MyFinanceNavKey) {
        viewModelScope.launch { _navEvents.send(key) }
    }

    fun showSnackBar(
        message: String,
        actionText: String? = null,
        actionFun: () -> Unit = {},
        dismissFun: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiEvents.emit(HomeUiEvent.ShowSnackBar(
                message,
                actionText,
                actionFun,
                dismissFun
            ))
        }
    }

    fun checkUser(userRequest: Boolean) {
        viewModelScope.launch {
            _uiEvents.emit(HomeUiEvent.LoadingStarted)
            val authResult = userRepository.isUserLogged()
            when (authResult.code) {
                AuthCode.USER_LOGGED.code -> {
                    updateUserData()
                    if (userRequest) {
                        _uiEvents.emit(HomeUiEvent.LoginSuccess)
                    }
                }
                AuthCode.USER_NOT_LOGGED.code -> {
                    _logicEvents.emit(HomeLogicEvent.UserNotLogged)
                }
            }
            _uiEvents.emit(HomeUiEvent.LoadingFinished)
        }
    }

    private suspend fun updateUserData() {
        expensesRepository.updateLocalMonthlyBudget()
        expensesRepository.updateLocalLabels()
        _logicEvents.emit(HomeLogicEvent.UserLocalDataLoaded)
        expensesRepository.updateExpensesList()
        incomeRepository.updateIncomeList()
        expensesRepository.getMonthlyBudget()
        expensesRepository.getLabels()
    }

    fun getFullName(): String {
        return userRepository.getUser()?.fullName ?: ""
    }

    fun getProPic(): String? {
        return userRepository.getProPic()
    }

    fun onLogoutButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModelScope.launch {
            val logoutResult = userRepository.userLogout()
            if (logoutResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                _logicEvents.emit(HomeLogicEvent.LogoutSuccess)
            }
        }
    }

    fun isDynamicColorOn(): Boolean {
        return expensesRepository.getDynamicColorActive()
    }
}
