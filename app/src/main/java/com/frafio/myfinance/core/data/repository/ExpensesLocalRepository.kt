package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpensesLocalRepository {

    fun getWithFilter(name: String, categories: List<Int>): Flow<List<Expense>>

    fun getWithFilterDate(name: String, categories: List<Int>, firstTimestamp: Long, lastTimestamp: Long): Flow<List<Expense>>

    fun getCount(): Flow<Int>

    fun getPriceSumFromDay(year: Int, month: Int, day: Int): Flow<Double?>

    fun getPriceSumFromMonth(year: Int, month: Int): Flow<Double?>

    fun getPriceSumFromYear(year: Int): Flow<Double?>

    fun getPriceSumAfterAndBefore(
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): Flow<List<BarChartEntry>>

    fun getEarliestYearMonth(): Flow<DatePoint?>

    fun getExpensesOfMonth(year: Int, month: Int): Flow<List<Expense>>

    fun getExpensesOfYear(year: Int): Flow<List<Expense>>

    fun getAllSync(): List<Expense>

    suspend fun getById(id: String): Expense?

    fun deleteAll()
}
