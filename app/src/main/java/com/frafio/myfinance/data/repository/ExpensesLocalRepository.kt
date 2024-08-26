package com.frafio.myfinance.data.repository

import androidx.lifecycle.LiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.model.BarChartEntry
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.storage.MyFinanceDatabase

class ExpensesLocalRepository {
    private val expenseDao =
        MyFinanceDatabase.getDatabase(MyFinanceApplication.instance).expenseDao()

    fun getAll(): LiveData<List<Expense>> = expenseDao.getAll()

    fun getCount(): LiveData<Int> = expenseDao.getCount()

    fun getPriceSumFromDay(year: Int, month: Int, day: Int): LiveData<Double?> =
        expenseDao.getPriceSumOfDay(year, month, day)

    fun getPriceSumFromMonth(year: Int, month: Int): LiveData<Double?> =
        expenseDao.getPriceSumOfMonth(year, month)

    fun getPriceSumFromYear(year: Int): LiveData<Double?> =
        expenseDao.getPriceSumOfYear(year)

    fun getPriceSumAfterAndBefore(
        firstTimestamp: Long,
        lastTimestamp: Long
    ): LiveData<List<BarChartEntry>> =
        expenseDao.getPriceSumAfterAndBefore(firstTimestamp, lastTimestamp)

    fun getExpensesOfMonth(year: Int, month: Int): LiveData<List<Expense>> =
        expenseDao.getExpensesOfMonth(year, month)

    fun getExpensesOfYear(year: Int): LiveData<List<Expense>> =
        expenseDao.getExpensesOfYear(year)

    fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)

    fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    fun deleteAll() = expenseDao.deleteAll()

    fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    fun updateTable(expenses: List<Expense>) = expenseDao.updateTable(*expenses.toTypedArray())
}