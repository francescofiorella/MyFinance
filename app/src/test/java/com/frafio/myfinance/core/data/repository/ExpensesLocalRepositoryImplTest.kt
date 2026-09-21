package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.dao.ExpenseDao
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class ExpensesLocalRepositoryImplTest {

    private val dao = RecordingExpenseDao()
    private val subject = ExpensesLocalRepositoryImpl(dao)

    @Test
    fun getWithFilter_passesPlainTextThrough() {
        subject.getWithFilter("coffee", listOf(5))
        assertThat(dao.lastName).isEqualTo("coffee")
    }

    @Test
    fun getWithFilter_escapesPercent() {
        subject.getWithFilter("50%", listOf(5))
        assertThat(dao.lastName).isEqualTo("50\\%")
    }

    @Test
    fun getWithFilter_escapesUnderscore() {
        subject.getWithFilter("a_b", listOf(5))
        assertThat(dao.lastName).isEqualTo("a\\_b")
    }

    @Test
    fun getWithFilter_escapesTheEscapeCharacterFirst() {
        // A backslash in the term must not turn a following % into an escape sequence.
        subject.getWithFilter("C:\\%", listOf(5))
        assertThat(dao.lastName).isEqualTo("C:\\\\\\%")
    }

    @Test
    fun getWithFilterDate_escapesTheSameWay() {
        subject.getWithFilterDate("100%_", listOf(5), 1L, 2L)
        assertThat(dao.lastName).isEqualTo("100\\%\\_")
    }

    /** Records the name it is queried with; every read returns an empty flow. */
    private class RecordingExpenseDao : ExpenseDao {
        var lastName: String? = null

        override fun getWithFilter(name: String, categories: List<Int>): Flow<List<Expense>> {
            lastName = name
            return flowOf(emptyList())
        }

        override fun getWithFilterDate(
            name: String,
            categories: List<Int>,
            firstTimestamp: Long,
            lastTimestamp: Long
        ): Flow<List<Expense>> {
            lastName = name
            return flowOf(emptyList())
        }

        override fun getCount(): Flow<Int> = flowOf(0)
        override fun getPriceSumOfDay(year: Int, month: Int, day: Int): Flow<Double?> = flowOf(null)
        override fun getPriceSumOfMonth(year: Int, month: Int): Flow<Double?> = flowOf(null)
        override fun getPriceSumOfYear(year: Int): Flow<Double?> = flowOf(null)
        override fun getEarliestYearMonth(): Flow<DatePoint?> = flowOf(null)
        override fun getPriceSumAfterAndBefore(
            startYear: Int,
            startMonth: Int,
            endYear: Int,
            endMonth: Int
        ): Flow<List<BarChartEntry>> = flowOf(emptyList())
        override fun getExpensesOfMonth(year: Int, month: Int): Flow<List<Expense>> = flowOf(emptyList())
        override fun getExpensesOfYear(year: Int): Flow<List<Expense>> = flowOf(emptyList())
        override suspend fun getById(id: String): Expense? = null
        override fun getAllSync(): List<Expense> = emptyList()
        override fun upsert(item: Expense) = Unit
        override fun insertAll(vararg items: Expense) = Unit
        override fun updateExpense(expense: Expense) = Unit
        override fun deleteExpense(expense: Expense) = Unit
        override fun deleteById(id: String) = Unit
        override fun deleteAll() = Unit
    }
}
