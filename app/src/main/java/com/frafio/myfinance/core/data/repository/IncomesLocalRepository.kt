package com.frafio.myfinance.core.data.repository

import kotlinx.coroutines.flow.Flow
import com.frafio.myfinance.core.data.dao.IncomeDao
import com.frafio.myfinance.core.data.model.Income
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomesLocalRepository @Inject constructor(
    private val incomeDao: IncomeDao
) {

    fun getAll(): Flow<List<Income>> = incomeDao.getAll()

    fun getCount(): Flow<Int> = incomeDao.getCount()

    fun getPriceSumFromYear(year: Int): Flow<Double?> =
        incomeDao.getPriceSumOfYear(year)

    fun insertIncome(income: Income) = incomeDao.insertIncome(income)

    fun updateIncome(income: Income) = incomeDao.updateIncome(income)

    fun deleteAll() = incomeDao.deleteAll()

    fun deleteIncome(income: Income) = incomeDao.deleteIncome(income)

    fun updateTable(incomes: List<Income>) = incomeDao.updateTable(*incomes.toTypedArray())
}