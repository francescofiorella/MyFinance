package com.frafio.myfinance.data.repository

import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.manager.ExpensesManager
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesRepository @Inject constructor(private val expensesManager: ExpensesManager) {
    suspend fun updateExpensesList(): FinanceResult {
        return expensesManager.updateExpensesList()
    }

    suspend fun deleteExpense(expense: Expense): FinanceResult {
        return expensesManager.deleteExpense(expense)
    }

    suspend fun addExpense(expense: Expense): FinanceResult {
        return expensesManager.addExpenses(expense)
    }

    suspend fun editExpense(expense: Expense): FinanceResult {
        return expensesManager.editExpense(expense)
    }

    fun setDynamicColorActive(active: Boolean) {
        expensesManager.setDynamicColorActive(active)
    }

    fun getDynamicColorActive(): Boolean {
        return expensesManager.getDynamicColorActive()
    }

    suspend fun getMonthlyBudget(): FinanceResult {
        return expensesManager.getMonthlyBudget()
    }

    suspend fun setMonthlyBudget(budget: Double): FinanceResult {
        return expensesManager.setMonthlyBudget(budget)
    }

    fun updateLocalMonthlyBudget() {
        expensesManager.updateLocalMonthlyBudget()
    }

    suspend fun getLabels(): FinanceResult {
        return expensesManager.getLabels()
    }

    suspend fun setLabels(
        labels: List<String>,
        successCode: FinanceCode = FinanceCode.LABELS_UPDATE_SUCCESS
    ): FinanceResult {
        return expensesManager.setLabels(labels, successCode)
    }

    fun updateLocalLabels() {
        expensesManager.updateLocalLabels()
    }
}