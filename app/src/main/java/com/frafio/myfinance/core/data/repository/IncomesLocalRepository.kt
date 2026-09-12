package com.frafio.myfinance.core.data.repository

import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Income
import kotlinx.coroutines.flow.Flow

interface IncomesLocalRepository {

    fun getAll(): Flow<List<Income>>

    fun getCount(): Flow<Int>

    fun getPriceSumFromYear(year: Int): Flow<Double?>

    fun getEarliestYearMonth(): Flow<DatePoint?>

    fun deleteAll()
}
