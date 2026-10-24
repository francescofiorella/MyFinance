package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.model.CategorySum
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.MonthlyTotal
import kotlinx.coroutines.flow.Flow

interface ExpensesLocalRepository {

    fun getWithFilter(name: String, categories: List<Int>): Flow<List<Expense>>

    fun getWithFilterDate(name: String, categories: List<Int>, firstTimestamp: Long, lastTimestamp: Long): Flow<List<Expense>>

    fun getCount(): Flow<Int>

    fun getPriceSumFromDay(year: Int, month: Int, day: Int): Flow<Double?>

    fun getPriceSumFromMonth(year: Int, month: Int): Flow<Double?>

    fun getPriceSumFromYear(year: Int): Flow<Double?>

    fun getCategorySumsOfMonth(year: Int, month: Int): Flow<List<CategorySum>>

    fun getPriceSumAfterAndBefore(
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): Flow<List<MonthlyTotal>>

    fun getEarliestYearMonth(): Flow<DatePoint?>

    fun getExpensesOfMonth(year: Int, month: Int): Flow<List<Expense>>

    fun getExpensesOfYear(year: Int): Flow<List<Expense>>

    /** Expenses of [category] from [startYear]/[startMonth] (inclusive) to [endYear]/[endMonth] (exclusive), newest first. */
    fun getExpensesOfCategory(category: Int, startYear: Int, startMonth: Int, endYear: Int, endMonth: Int): Flow<List<Expense>>

    fun getAllSync(): List<Expense>

    suspend fun getById(id: String): Expense?

    fun deleteAll()
}
