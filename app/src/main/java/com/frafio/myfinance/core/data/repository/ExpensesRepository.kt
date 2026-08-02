package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.manager.ExpensesSyncManager
import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesRepository @Inject constructor(private val expensesManager: ExpensesSyncManager) {
    private var lastDeletedLabel: String? = null
    private var lastAffectedExpenses: List<Expense> = emptyList()

    suspend fun deleteExpense(expense: Expense): FinanceResult {
        return expensesManager.delete(expense)
    }

    suspend fun addExpense(expense: Expense): FinanceResult {
        return expensesManager.add(expense)
    }

    suspend fun editExpense(expense: Expense): FinanceResult {
        return expensesManager.edit(expense)
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

    suspend fun addLabel(label: String): FinanceResult {
        return expensesManager.addLabel(label)
    }

    suspend fun deleteLabel(label: String): DeleteLabelResult {
        val result = expensesManager.deleteLabel(label)
        if (result.financeResult.code == FinanceCode.LABEL_DELETE_SUCCESS.code) {
            lastDeletedLabel = label
            lastAffectedExpenses = result.affectedExpenses
        }
        return result
    }

    suspend fun editLabel(oldName: String, newName: String): FinanceResult {
        return expensesManager.editLabel(oldName, newName)
    }

    suspend fun undoDeleteLabel(): FinanceResult {
        val label = lastDeletedLabel ?: return FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        val result = expensesManager.undoDeleteLabel(label, lastAffectedExpenses)
        if (result.code == FinanceCode.LABELS_UPDATE_SUCCESS.code) {
            resetLastDeletedLabel()
        }
        return result
    }

    fun resetLastDeletedLabel() {
        lastDeletedLabel = null
        lastAffectedExpenses = emptyList()
    }

    fun startSnapshotListener(scope: CoroutineScope) {
        expensesManager.startSnapshotListener(scope)
    }

    fun stopSnapshotListener() {
        expensesManager.stopSnapshotListener()
    }
}
