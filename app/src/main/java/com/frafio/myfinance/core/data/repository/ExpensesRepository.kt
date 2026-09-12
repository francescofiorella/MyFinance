package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope

interface ExpensesRepository {

    suspend fun deleteExpense(expense: Expense): FinanceResult

    suspend fun addExpense(expense: Expense): FinanceResult

    suspend fun editExpense(expense: Expense): FinanceResult

    suspend fun setDynamicColorActive(active: Boolean)

    suspend fun setMonthlyBudget(budget: Double): FinanceResult

    suspend fun setCurrencyCode(currencyCode: String): FinanceResult

    suspend fun setProPicChoice(choice: String): FinanceResult

    suspend fun updateExpenseLabels(expenseId: String, label: String, isAddition: Boolean): FinanceResult

    suspend fun addLabel(label: String): FinanceResult

    suspend fun deleteLabel(label: String): DeleteLabelResult

    suspend fun editLabel(oldName: String, newName: String): FinanceResult

    suspend fun undoDeleteLabel(label: String, affectedExpenses: List<Expense>): FinanceResult

    fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>? = null
    )

    fun startRootSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>? = null
    )

    fun stopSnapshotListener()
}
