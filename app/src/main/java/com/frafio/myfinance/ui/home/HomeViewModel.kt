package com.frafio.myfinance.ui.home

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.repository.UserRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(
        (application as MyFinanceApplication).authManager
    )
    private val expensesRepository = ExpensesRepository(
        (application as MyFinanceApplication).expensesManager
    )
    private val incomeRepository = IncomeRepository(
        (application as MyFinanceApplication).incomesManager
    )
    var listener: HomeListener? = null

    val fragmentStack = mutableListOf<String>()

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
}