package com.frafio.myfinance.ui.home

import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.IncomesLocalRepository
import com.frafio.myfinance.data.repository.LoadingRepository
import com.frafio.myfinance.data.repository.UserPreferencesData
import com.frafio.myfinance.data.repository.UserRepository
import com.frafio.myfinance.data.repository.UserPreferencesRepository
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val expensesRepository: ExpensesRepository,
    private val incomeRepository: IncomeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val expensesLocalRepository: ExpensesLocalRepository,
    private val incomesLocalRepository: IncomesLocalRepository,
    private val loadingRepository: LoadingRepository,
    profileImageStorage: com.frafio.myfinance.data.storage.ProfileImageStorage
) : ViewModel() {

    val isLoading: StateFlow<Boolean> = loadingRepository.isLoading

    val profilePicture: StateFlow<Bitmap?> = userRepository.profilePicture
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), profileImageStorage.loadBitmapSync())

    val userPreferences: StateFlow<UserPreferencesData?> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val user: StateFlow<User?> = userPreferences
        .map { it?.user }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _navEvents = Channel<MyFinanceNavKey>(Channel.BUFFERED)
    val navEvents: Flow<MyFinanceNavKey> = _navEvents.receiveAsFlow()

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
            try {
                loadingRepository.startLoading()
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
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    private suspend fun updateUserData() {
        // Wait for first local emissions to ensure screen can be populated
        val userPrefs = userPreferencesRepository.userPreferencesFlow.first()
        expensesLocalRepository.getCount().first()
        incomesLocalRepository.getCount().first()

        _logicEvents.emit(HomeLogicEvent.UserLocalDataLoaded)

        // Remote sync in background
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                userRepository.syncProfilePicture(userPrefs.user?.photoUrl)
                expensesRepository.updateExpensesList()
                incomeRepository.updateIncomeList()
                expensesRepository.getMonthlyBudget()
                expensesRepository.getLabels()
            } finally {
                loadingRepository.stopLoading()
            }
        }
    }

    fun getFullName(): String {
        return user.value?.fullName ?: ""
    }

    fun onLogoutButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModelScope.launch {
            val logoutResult = userRepository.userLogout()
            if (logoutResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                _logicEvents.emit(HomeLogicEvent.LogoutSuccess)
            }
        }
    }
}
