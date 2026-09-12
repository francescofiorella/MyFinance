package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.dao.ExpenseDao
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesLocalRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpensesLocalRepository {

    override fun getWithFilter(name: String, categories: List<Int>): Flow<List<Expense>> =
        expenseDao.getWithFilter(name, categories)

    override fun getWithFilterDate(name: String, categories: List<Int>, firstTimestamp: Long, lastTimestamp: Long): Flow<List<Expense>> =
        expenseDao.getWithFilterDate(name, categories, firstTimestamp, lastTimestamp)

    override fun getCount(): Flow<Int> = expenseDao.getCount()

    override fun getPriceSumFromDay(year: Int, month: Int, day: Int): Flow<Double?> =
        expenseDao.getPriceSumOfDay(year, month, day)

    override fun getPriceSumFromMonth(year: Int, month: Int): Flow<Double?> =
        expenseDao.getPriceSumOfMonth(year, month)

    override fun getPriceSumFromYear(year: Int): Flow<Double?> =
        expenseDao.getPriceSumOfYear(year)

    override fun getPriceSumAfterAndBefore(
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int
    ): Flow<List<BarChartEntry>> =
        expenseDao.getPriceSumAfterAndBefore(startYear, startMonth, endYear, endMonth)

    override fun getEarliestYearMonth(): Flow<DatePoint?> = expenseDao.getEarliestYearMonth()

    override fun getExpensesOfMonth(year: Int, month: Int): Flow<List<Expense>> =
        expenseDao.getExpensesOfMonth(year, month)

    override fun getExpensesOfYear(year: Int): Flow<List<Expense>> =
        expenseDao.getExpensesOfYear(year)

    override fun getAllSync(): List<Expense> = expenseDao.getAllSync()

    override suspend fun getById(id: String): Expense? = expenseDao.getById(id)

    override fun deleteAll() = expenseDao.deleteAll()
}