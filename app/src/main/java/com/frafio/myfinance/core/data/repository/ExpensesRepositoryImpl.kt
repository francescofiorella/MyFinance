package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.manager.ExpensesSyncManager
import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesRepositoryImpl @Inject constructor(private val expensesManager: ExpensesSyncManager) : ExpensesRepository {

    override suspend fun deleteExpense(expense: Expense): FinanceResult {
        return expensesManager.delete(expense)
    }

    override suspend fun addExpense(expense: Expense): FinanceResult {
        return expensesManager.add(expense)
    }

    override suspend fun editExpense(expense: Expense): FinanceResult {
        return expensesManager.edit(expense)
    }

    override suspend fun setDynamicColorActive(active: Boolean) {
        expensesManager.setDynamicColorActive(active)
    }

    override suspend fun setMonthlyBudget(budget: Double): FinanceResult {
        return expensesManager.setMonthlyBudget(budget)
    }

    override suspend fun setCurrencyCode(currencyCode: String): FinanceResult {
        return expensesManager.setCurrencyCode(currencyCode)
    }

    override suspend fun setProPicChoice(choice: String): FinanceResult {
        return expensesManager.setProPicChoice(choice)
    }

    override suspend fun updateExpenseLabels(expenseId: String, label: String, isAddition: Boolean): FinanceResult {
        return expensesManager.updateExpenseLabels(expenseId, label, isAddition)
    }

    override suspend fun addLabel(label: String): FinanceResult {
        return expensesManager.addLabel(label)
    }

    override suspend fun deleteLabel(label: String): DeleteLabelResult {
        return expensesManager.deleteLabel(label)
    }

    override suspend fun editLabel(oldName: String, newName: String): FinanceResult {
        return expensesManager.editLabel(oldName, newName)
    }

    override suspend fun undoDeleteLabel(label: String, affectedExpenses: List<Expense>): FinanceResult {
        return expensesManager.undoDeleteLabel(label, affectedExpenses)
    }

    override fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>?
    ) {
        expensesManager.startSnapshotListener(scope, onInitialSync)
    }

    override fun startRootSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>?
    ) {
        expensesManager.startRootSnapshotListener(scope, onInitialSync)
    }

    override fun stopSnapshotListener() {
        expensesManager.stopSnapshotListener()
    }
}
