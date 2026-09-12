package com.frafio.myfinance.testing.repository

import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-in for the Room-backed repository. Query methods replicate the DAO's
 * filtering and ordering so ViewModel tests exercise the same shapes the app sees.
 * Nothing is emitted until [sendExpenses] is called, so `stateIn` initial values are observable.
 */
class TestExpensesLocalRepository : ExpensesLocalRepository {

    data class FilterCall(
        val name: String,
        val categories: List<Int>,
        val firstTimestamp: Long? = null,
        val lastTimestamp: Long? = null,
    )

    private val expensesFlow: MutableSharedFlow<List<Expense>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val filterCalls = mutableListOf<FilterCall>()
    var deleteAllCalled = false
        private set

    override fun getWithFilter(name: String, categories: List<Int>): Flow<List<Expense>> {
        filterCalls += FilterCall(name, categories)
        return expensesFlow.map { expenses ->
            expenses.filter { it.matches(name, categories) }.sortedForList()
        }
    }

    override fun getWithFilterDate(
        name: String,
        categories: List<Int>,
        firstTimestamp: Long,
        lastTimestamp: Long
    ): Flow<List<Expense>> {
        filterCalls += FilterCall(name, categories, firstTimestamp, lastTimestamp)
        return expensesFlow.map { expenses ->
            expenses.filter {
                it.matches(name, categories) &&
                    (it.timestamp ?: 0L) >= firstTimestamp &&
                    (it.timestamp ?: 0L) < lastTimestamp
            }.sortedForList()
        }
    }

    override fun getCount(): Flow<Int> = expensesFlow.map { it.size }

    override fun getPriceSumFromDay(year: Int, month: Int, day: Int): Flow<Double?> =
        expensesFlow.map { expenses ->
            expenses.filter { it.year == year && it.month == month && it.day == day }.sumOrNull()
        }

    override fun getPriceSumFromMonth(year: Int, month: Int): Flow<Double?> =
        expensesFlow.map { expenses ->
            expenses.filter { it.year == year && it.month == month }.sumOrNull()
        }

    override fun getPriceSumFromYear(year: Int): Flow<Double?> =
        expensesFlow.map { expenses -> expenses.filter { it.year == year }.sumOrNull() }

    override fun getPriceSumAfterAndBefore(
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): Flow<List<BarChartEntry>> = expensesFlow.map { expenses ->
        val start = startYear * 100 + startMonth
        val end = endYear * 100 + endMonth
        expenses
            .filter { it.year != null && it.month != null }
            .filter { (it.year!! * 100 + it.month!!) in start until end }
            .groupBy { it.year!! to it.month!! }
            .map { (yearMonth, group) ->
                BarChartEntry(group.sumOf { it.price ?: 0.0 }, yearMonth.first, yearMonth.second)
            }
            .sortedWith(compareByDescending<BarChartEntry> { it.year }.thenByDescending { it.month })
    }

    override fun getEarliestYearMonth(): Flow<DatePoint?> = expensesFlow.map { expenses ->
        expenses
            .filter { it.year != null && it.month != null }
            .minByOrNull { it.year!! * 100 + it.month!! }
            ?.let { DatePoint(it.year!!, it.month!!) }
    }

    override fun getExpensesOfMonth(year: Int, month: Int): Flow<List<Expense>> =
        expensesFlow.map { expenses -> expenses.filter { it.year == year && it.month == month } }

    override fun getExpensesOfYear(year: Int): Flow<List<Expense>> =
        expensesFlow.map { expenses -> expenses.filter { it.year == year } }

    override fun getAllSync(): List<Expense> = expensesFlow.replayCache.firstOrNull() ?: emptyList()

    override suspend fun getById(id: String): Expense? = getAllSync().find { it.id == id }

    override fun deleteAll() {
        deleteAllCalled = true
        expensesFlow.tryEmit(emptyList())
    }

    /**
     * A test-only API to allow controlling the list of expenses from tests.
     */
    fun sendExpenses(expenses: List<Expense>) {
        expensesFlow.tryEmit(expenses)
    }

    fun sendExpenses(vararg expenses: Expense) = sendExpenses(expenses.toList())

    // Mirrors the DAO's prefix-or-word-start LIKE predicate.
    private fun Expense.matches(name: String, categories: List<Int>): Boolean {
        val expenseName = this.name.orEmpty()
        val nameMatches = expenseName.startsWith(name, ignoreCase = true) ||
            expenseName.contains(" $name", ignoreCase = true)
        return nameMatches && category in categories
    }

    // ORDER BY year DESC, month DESC, day DESC, price DESC, category DESC
    private fun List<Expense>.sortedForList(): List<Expense> = sortedWith(
        compareByDescending<Expense> { it.year ?: Int.MIN_VALUE }
            .thenByDescending { it.month ?: Int.MIN_VALUE }
            .thenByDescending { it.day ?: Int.MIN_VALUE }
            .thenByDescending { it.price ?: Double.MIN_VALUE }
            .thenByDescending { it.category ?: Int.MIN_VALUE }
    )

    // SUM(price) is NULL when no rows match.
    private fun List<Expense>.sumOrNull(): Double? =
        mapNotNull { it.price }.takeIf { it.isNotEmpty() }?.sum()
}
