package com.frafio.myfinance.ui.home

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.UserRepository
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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

    private val _navEvents = Channel<MyFinanceNavKey>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    fun navigateTo(key: MyFinanceNavKey) {
        viewModelScope.launch { _navEvents.send(key) }
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
        return expensesRepository.getDynamicColorActive()
    }

    fun setDynamicColor(enabled: Boolean) {
        expensesRepository.setDynamicColorActive(enabled)
    }

    fun updateLocalUser() {
        // user state is managed in MyFinanceStorage/repositories
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
