package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.manager.ExpensesManager
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
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

    suspend fun setDynamicColorActive(active: Boolean) {
        expensesManager.setDynamicColorActive(active)
    }

    suspend fun getMonthlyBudget(): FinanceResult {
        return expensesManager.getMonthlyBudget()
    }

    suspend fun setMonthlyBudget(budget: Double): FinanceResult {
        return expensesManager.setMonthlyBudget(budget)
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
}