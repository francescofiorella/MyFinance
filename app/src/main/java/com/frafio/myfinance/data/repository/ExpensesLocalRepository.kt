package com.frafio.myfinance.data.repository

import kotlinx.coroutines.flow.Flow
import com.frafio.myfinance.data.dao.ExpenseDao
import com.frafio.myfinance.data.model.BarChartEntry
import com.frafio.myfinance.data.model.Expense
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpensesLocalRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {

    fun getWithFilter(name: String, categories: List<Int>): Flow<List<Expense>> =
        expenseDao.getWithFilter(name, categories)

    fun getWithFilterDate(name: String, categories: List<Int>, firstTimestamp: Long, lastTimestamp: Long): Flow<List<Expense>> =
        expenseDao.getWithFilterDate(name, categories, firstTimestamp, lastTimestamp)

    fun getCount(): Flow<Int> = expenseDao.getCount()

    fun getPriceSumFromDay(year: Int, month: Int, day: Int): Flow<Double?> =
        expenseDao.getPriceSumOfDay(year, month, day)

    fun getPriceSumFromMonth(year: Int, month: Int): Flow<Double?> =
        expenseDao.getPriceSumOfMonth(year, month)

    fun getPriceSumFromYear(year: Int): Flow<Double?> =
        expenseDao.getPriceSumOfYear(year)

    fun getPriceSumAfterAndBefore(
        firstTimestamp: Long,
        lastTimestamp: Long
    ): Flow<List<BarChartEntry>> =
        expenseDao.getPriceSumAfterAndBefore(firstTimestamp, lastTimestamp)

    fun getExpensesOfMonth(year: Int, month: Int): Flow<List<Expense>> =
        expenseDao.getExpensesOfMonth(year, month)

    fun getExpensesOfYear(year: Int): Flow<List<Expense>> =
        expenseDao.getExpensesOfYear(year)

    fun getAllSync(): List<Expense> = expenseDao.getAllSync()

    fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)

    fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    fun deleteAll() = expenseDao.deleteAll()

    fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    fun updateTable(expenses: List<Expense>) = expenseDao.updateTable(*expenses.toTypedArray())
}