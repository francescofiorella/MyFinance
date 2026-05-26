package com.frafio.myfinance.data.repository

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.manager.ExpensesManager
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesRepository @Inject constructor(private val expensesManager: ExpensesManager) {
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

    fun getLabels(): LiveData<FinanceResult> {
        return expensesManager.getLabels()
    }

    fun setLabels(
        labels: List<String>,
        successCode: FinanceCode = FinanceCode.LABELS_UPDATE_SUCCESS
    ): LiveData<FinanceResult> {
        return expensesManager.setLabels(labels, successCode)
    }

    fun updateLocalLabels() {
        expensesManager.updateLocalLabels()
    }
}