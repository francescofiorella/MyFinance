package com.frafio.myfinance.testing.repository

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.ExpensesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope

/**
 * Records every write and answers with a configurable [FinanceResult] per operation.
 * Defaults are the success codes the ViewModels branch on.
 */
class TestExpensesRepository : ExpensesRepository {

    var addResult = FinanceResult(FinanceCode.EXPENSE_ADD_SUCCESS)
    var editResult = FinanceResult(FinanceCode.EXPENSE_EDIT_SUCCESS)
    var deleteResult = FinanceResult(FinanceCode.EXPENSE_DELETE_SUCCESS)
    var budgetResult = FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
    var currencyResult = FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
    var proPicResult = FinanceResult(FinanceCode.BUDGET_UPDATE_SUCCESS)
    var expenseLabelsResult = FinanceResult(FinanceCode.EXPENSE_EDIT_SUCCESS)
    var addLabelResult = FinanceResult(FinanceCode.LABEL_ADD_SUCCESS)
    var deleteLabelResult = DeleteLabelResult(FinanceResult(FinanceCode.LABEL_DELETE_SUCCESS))
    var editLabelResult = FinanceResult(FinanceCode.LABEL_UPDATE_SUCCESS)
    var undoDeleteLabelResult = FinanceResult(FinanceCode.LABELS_UPDATE_SUCCESS)

    val addedExpenses = mutableListOf<Expense>()
    val editedExpenses = mutableListOf<Expense>()
    val deletedExpenses = mutableListOf<Expense>()
    val dynamicColorCalls = mutableListOf<Boolean>()
    val budgetCalls = mutableListOf<Double>()
    val currencyCodeCalls = mutableListOf<String>()
    val proPicChoiceCalls = mutableListOf<String>()
    val expenseLabelCalls = mutableListOf<Triple<String, String, Boolean>>()
    val addedLabels = mutableListOf<String>()
    val deletedLabels = mutableListOf<String>()
    val editedLabels = mutableListOf<Pair<String, String>>()
    val undoneLabels = mutableListOf<Pair<String, List<Expense>>>()

    /** When false, the deferred handed to [startSnapshotListener] is left pending. */
    var completeInitialSyncImmediately = true
    var snapshotListenerRunning = false
        private set
    var rootSnapshotListenerRunning = false
        private set
    var stopCallCount = 0
        private set

    override suspend fun deleteExpense(expense: Expense): FinanceResult {
        deletedExpenses += expense
        return deleteResult
    }

    override suspend fun addExpense(expense: Expense): FinanceResult {
        addedExpenses += expense
        return addResult
    }

    override suspend fun editExpense(expense: Expense): FinanceResult {
        editedExpenses += expense
        return editResult
    }

    override suspend fun setDynamicColorActive(active: Boolean) {
        dynamicColorCalls += active
    }

    override suspend fun setMonthlyBudget(budget: Double): FinanceResult {
        budgetCalls += budget
        return budgetResult
    }

    override suspend fun setCurrencyCode(currencyCode: String): FinanceResult {
        currencyCodeCalls += currencyCode
        return currencyResult
    }

    override suspend fun setProPicChoice(choice: String): FinanceResult {
        proPicChoiceCalls += choice
        return proPicResult
    }

    override suspend fun updateExpenseLabels(
        expenseId: String,
        label: String,
        isAddition: Boolean
    ): FinanceResult {
        expenseLabelCalls += Triple(expenseId, label, isAddition)
        return expenseLabelsResult
    }

    override suspend fun addLabel(label: String): FinanceResult {
        addedLabels += label
        return addLabelResult
    }

    override suspend fun deleteLabel(label: String): DeleteLabelResult {
        deletedLabels += label
        return deleteLabelResult
    }

    override suspend fun editLabel(oldName: String, newName: String): FinanceResult {
        editedLabels += oldName to newName
        return editLabelResult
    }

    override suspend fun undoDeleteLabel(label: String, affectedExpenses: List<Expense>): FinanceResult {
        undoneLabels += label to affectedExpenses
        return undoDeleteLabelResult
    }

    override fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>?
    ) {
        snapshotListenerRunning = true
        if (completeInitialSyncImmediately) onInitialSync?.complete(Unit)
    }

    override fun startRootSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>?
    ) {
        rootSnapshotListenerRunning = true
        if (completeInitialSyncImmediately) onInitialSync?.complete(Unit)
    }

    override fun stopSnapshotListener() {
        stopCallCount++
        snapshotListenerRunning = false
        rootSnapshotListenerRunning = false
    }
}
