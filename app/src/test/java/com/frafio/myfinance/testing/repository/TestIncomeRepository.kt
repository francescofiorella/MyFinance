package com.frafio.myfinance.testing.repository

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.repository.IncomeRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope

/**
 * Records every write and answers with a configurable [FinanceResult] per operation;
 * see [TestExpensesRepository].
 */
class TestIncomeRepository : IncomeRepository {

    var addResult = FinanceResult(FinanceCode.INCOME_ADD_SUCCESS)
    var editResult = FinanceResult(FinanceCode.INCOME_EDIT_SUCCESS)
    var deleteResult = FinanceResult(FinanceCode.INCOME_DELETE_SUCCESS)

    val addedIncomes = mutableListOf<Income>()
    val editedIncomes = mutableListOf<Income>()
    val deletedIncomes = mutableListOf<Income>()

    /** When false, the deferred handed to [startSnapshotListener] is left pending. */
    var completeInitialSyncImmediately = true
    var errorToEmit: FirebaseFirestoreException? = null
    var snapshotListenerRunning = false
        private set
    var stopCallCount = 0
        private set

    override suspend fun addIncome(income: Income): FinanceResult {
        addedIncomes += income
        return addResult
    }

    override suspend fun editIncome(income: Income): FinanceResult {
        editedIncomes += income
        return editResult
    }

    override suspend fun deleteIncome(income: Income): FinanceResult {
        deletedIncomes += income
        return deleteResult
    }

    override fun startSnapshotListener(
        scope: CoroutineScope,
        onInitialSync: CompletableDeferred<Unit>?,
        onError: ((FirebaseFirestoreException) -> Unit)?
    ) {
        snapshotListenerRunning = true
        errorToEmit?.let { onError?.invoke(it) }
        if (completeInitialSyncImmediately) onInitialSync?.complete(Unit)
    }

    override fun stopSnapshotListener() {
        stopCallCount++
        snapshotListenerRunning = false
    }
}
