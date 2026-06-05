package com.frafio.myfinance.app

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.navigation.HomeTabKey
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.ExpensesRepository
import com.frafio.myfinance.core.data.repository.IncomeRepository
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.repository.UserRepository
import com.frafio.myfinance.core.data.storage.ProfileImageStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

sealed class MainEvent {
    object UserNotLogged : MainEvent()
    object LogoutSuccess : MainEvent()
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

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Complete : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    val userRepository: UserRepository,
    private val expensesRepository: ExpensesRepository,
    private val incomeRepository: IncomeRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val expensesLocalRepository: ExpensesLocalRepository,
    private val incomesLocalRepository: IncomesLocalRepository,
    private val loadingRepository: LoadingRepository,
    profileImageStorage: ProfileImageStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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

    private val _navEvents = Channel<NavKey>(Channel.BUFFERED)
    val navEvents: Flow<NavKey> = _navEvents.receiveAsFlow()

    private val _scrollEvents = MutableSharedFlow<Pair<String, Boolean>?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val scrollEvents: Flow<Pair<String, Boolean>?> = _scrollEvents.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<HomeUiEvent>()
    val uiEvents: SharedFlow<HomeUiEvent> = _uiEvents.asSharedFlow()

    private val _mainEvents = MutableSharedFlow<MainEvent>(replay = 1)
    val mainEvents: SharedFlow<MainEvent> = _mainEvents.asSharedFlow()

    fun navigateTo(key: NavKey) {
        viewModelScope.launch { _navEvents.send(key) }
    }

    fun onTransactionCommitted(isExpense: Boolean, day: Int, month: Int, year: Int) {
        viewModelScope.launch {
            val targetTab = if (isExpense) HomeTabKey.Expenses else HomeTabKey.Budget
            navigateTo(targetTab)

            val scrollId = if (isExpense) {
                "total_${day}_${month}_${year}"
            } else {
                year.toString()
            }

            _scrollEvents.emit(scrollId to isExpense)
        }
    }

    fun resetScrollEvent() {
        viewModelScope.launch {
            _scrollEvents.emit(null)
        }
    }

    fun checkUser(notify: Boolean) {
        viewModelScope.launch {
            try {
                loadingRepository.startLoading()
                val authResult = userRepository.isUserLogged()
                when (authResult.code) {
                    AuthCode.USER_LOGGED.code -> {
                        updateUserData()
                        if (notify) {
                            _uiEvents.emit(HomeUiEvent.LoginSuccess)
                        }
                    }

                    AuthCode.USER_NOT_LOGGED.code -> {
                        _uiState.value = HomeUiState.Complete
                        _mainEvents.emit(MainEvent.UserNotLogged)
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

        _uiState.value = HomeUiState.Complete

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

    fun onLogoutButtonClick() {
        viewModelScope.launch {
            val logoutResult = userRepository.userLogout()
            if (logoutResult.code == AuthCode.LOGOUT_SUCCESS.code) {
                _mainEvents.emit(MainEvent.LogoutSuccess)
            }
        }
    }
}
