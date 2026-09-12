package com.frafio.myfinance.core.data.repository

import kotlinx.coroutines.flow.Flow
import com.frafio.myfinance.core.data.dao.IncomeDao
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Income
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomesLocalRepositoryImpl @Inject constructor(
    private val incomeDao: IncomeDao
) : IncomesLocalRepository {

    override fun getAll(): Flow<List<Income>> = incomeDao.getAll()

    override fun getCount(): Flow<Int> = incomeDao.getCount()

    override fun getPriceSumFromYear(year: Int): Flow<Double?> =
        incomeDao.getPriceSumOfYear(year)

    override fun getEarliestYearMonth(): Flow<DatePoint?> = incomeDao.getEarliestYearMonth()

    override fun deleteAll() = incomeDao.deleteAll()
}