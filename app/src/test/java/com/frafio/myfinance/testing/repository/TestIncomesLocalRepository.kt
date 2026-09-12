package com.frafio.myfinance.testing.repository

import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for the Room-backed repository; see [TestExpensesLocalRepository].
 */
class TestIncomesLocalRepository : IncomesLocalRepository {

    private val incomesFlow: MutableSharedFlow<List<Income>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    var deleteAllCalled = false
        private set

    // ORDER BY year DESC, month DESC, day DESC, price DESC
    override fun getAll(): Flow<List<Income>> = incomesFlow.map { incomes ->
        incomes.sortedWith(
            compareByDescending<Income> { it.year ?: Int.MIN_VALUE }
                .thenByDescending { it.month ?: Int.MIN_VALUE }
                .thenByDescending { it.day ?: Int.MIN_VALUE }
                .thenByDescending { it.price ?: Double.MIN_VALUE }
        )
    }

    override fun getCount(): Flow<Int> = incomesFlow.map { it.size }

    override fun getPriceSumFromYear(year: Int): Flow<Double?> = incomesFlow.map { incomes ->
        incomes.filter { it.year == year }.mapNotNull { it.price }.takeIf { it.isNotEmpty() }?.sum()
    }

    override fun getEarliestYearMonth(): Flow<DatePoint?> = incomesFlow.map { incomes ->
        incomes
            .filter { it.year != null && it.month != null }
            .minByOrNull { it.year!! * 100 + it.month!! }
            ?.let { DatePoint(it.year!!, it.month!!) }
    }

    override fun deleteAll() {
        deleteAllCalled = true
        incomesFlow.tryEmit(emptyList())
    }

    /**
     * A test-only API to allow controlling the list of incomes from tests.
     */
    fun sendIncomes(incomes: List<Income>) {
        incomesFlow.tryEmit(incomes)
    }

    fun sendIncomes(vararg incomes: Income) = sendIncomes(incomes.toList())
}
