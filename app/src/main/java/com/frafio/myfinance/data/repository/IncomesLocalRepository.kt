package com.frafio.myfinance.data.repository

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.storage.MyFinanceDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomesLocalRepository @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
) {
    private val incomeDao =
        MyFinanceDatabase.getDatabase(context).incomeDao()

    fun getAll(): LiveData<List<Income>> = incomeDao.getAll()

    fun getPriceSumFromYear(year: Int): LiveData<Double?> =
        incomeDao.getPriceSumOfYear(year)

    fun insertIncome(income: Income) = incomeDao.insertIncome(income)

    fun updateIncome(income: Income) = incomeDao.updateIncome(income)

    fun deleteAll() = incomeDao.deleteAll()

    fun deleteIncome(income: Income) = incomeDao.deleteIncome(income)

    fun updateTable(incomes: List<Income>) = incomeDao.updateTable(*incomes.toTypedArray())
}