package com.frafio.myfinance.ui.home

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.R
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(
        (application as MyFinanceApplication).authManager,
    )
    private val expensesRepository = ExpensesRepository(
        (application as MyFinanceApplication).expensesManager
    )
    private val incomeRepository = IncomeRepository(
        (application as MyFinanceApplication).incomesManager
    )
    var listener: HomeListener? = null

    enum class Screen(val titleRes: Int) {
        DASHBOARD(R.string.dashboard),
        EXPENSES(R.string.expenses),
        BUDGET(R.string.budget),
        PROFILE(R.string.profile)
    }

    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _navigationStack = MutableStateFlow(listOf(Screen.DASHBOARD))

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value == screen) return
        
        val newStack = _navigationStack.value.toMutableList()
        newStack.add(screen)
        _navigationStack.value = newStack
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        val currentStack = _navigationStack.value
        if (currentStack.size > 1) {
            val newStack = currentStack.dropLast(1)
            _navigationStack.value = newStack
            _currentScreen.value = newStack.last()
            return true
        }
        return false
    }

    fun getNavigationStackSize(): Int {
        return _navigationStack.value.size
    }

    fun checkUser() {
        listener?.onSplashOperationComplete(userRepository.isUserLogged())
    }

    fun getProPic(): String? {
        return userRepository.getProPic()
    }

    fun onLogoutButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val logoutResponse = userRepository.userLogout()
        listener?.onLogOutSuccess(logoutResponse)
    }

    fun isDynamicColorOn(): Boolean {
        return userRepository.isDynamicColorOn()
    }

    fun updateUserExpenses() {
        listener?.onUserDataUpdated(expensesRepository.updateExpensesList())
    }

    fun updateUserIncomes() {
        listener?.onUserDataUpdated(incomeRepository.updateIncomeList())
    }

    fun updateMonthlyBudget() {
        val response = expensesRepository.getMonthlyBudget()
        listener?.onUserDataUpdated(response)
    }

    fun updateLocalMonthlyBudget() {
        expensesRepository.updateLocalMonthlyBudget()
    }

    fun updateLabels() {
        val response = expensesRepository.getLabels()
        listener?.onUserDataUpdated(response)
    }

    fun updateLocalLabels() {
        expensesRepository.updateLocalLabels()
    }
}