package com.frafio.myfinance.data.repository

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.manager.ExpensesManager
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult

class ExpensesRepository(private val expensesManager: ExpensesManager) {
    fun updateExpensesList(): LiveData<FinanceResult> {
        return expensesManager.updateExpensesList()
    }

    fun deleteExpense(expense: Expense): LiveData<FinanceResult> {
        return expensesManager.deleteExpense(expense)
    }

    fun addExpense(expense: Expense): LiveData<FinanceResult> {
        return expensesManager.addExpenses(expense)
    }

    fun editExpense(expense: Expense): LiveData<FinanceResult> {
        return expensesManager.editExpense(expense)
    }

    fun setDynamicColorActive(active: Boolean) {
        expensesManager.setDynamicColorActive(active)
    }

    fun getDynamicColorActive(): Boolean {
        return expensesManager.getDynamicColorActive()
    }

    fun getMonthlyBudget(): LiveData<FinanceResult> {
        return expensesManager.getMonthlyBudget()
    }

    fun setMonthlyBudget(budget: Double): LiveData<FinanceResult> {
        return expensesManager.setMonthlyBudget(budget)
    }

    fun updateLocalMonthlyBudget() {
        expensesManager.updateLocalMonthlyBudget()
    }
}