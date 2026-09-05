package com.frafio.myfinance.core.data.dao

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense

@Dao
interface ExpenseDao : BaseDao<Expense> {
    @Query("SELECT * " +
            "FROM expense " +
            "WHERE (name LIKE :name || '%' OR name LIKE '% ' || :name || '%') AND category IN (:categories) " +
            "ORDER BY year DESC, month DESC, day DESC, price DESC, category DESC")
    fun getWithFilter(name: String, categories: List<Int>): Flow<List<Expense>>

    @Query("SELECT * " +
            "FROM expense " +
            "WHERE (name LIKE :name || '%' OR name LIKE '% ' || :name || '%') AND category IN (:categories) AND timestamp>=:firstTimestamp AND timestamp<:lastTimestamp " +
            "ORDER BY year DESC, month DESC, day DESC, price DESC, category DESC")
    fun getWithFilterDate(name: String, categories: List<Int>, firstTimestamp: Long, lastTimestamp: Long): Flow<List<Expense>>

    @Query("SELECT COUNT(*) " +
            "FROM expense")
    fun getCount(): Flow<Int>

    @Query("SELECT SUM(price) " +
            "FROM expense " +
            "WHERE year=:year AND month=:month AND day=:day")
    fun getPriceSumOfDay(year: Int, month: Int, day: Int): Flow<Double?>

    @Query("SELECT SUM(price) " +
            "FROM expense " +
            "WHERE year=:year AND month=:month")
    fun getPriceSumOfMonth(year: Int, month: Int): Flow<Double?>

    @Query("SELECT SUM(price) " +
            "FROM expense " +
            "WHERE year=:year")
    fun getPriceSumOfYear(year: Int): Flow<Double?>

    @Query("SELECT year, month FROM expense ORDER BY year ASC, month ASC LIMIT 1")
    fun getEarliestYearMonth(): Flow<DatePoint?>

    @Query("SELECT SUM(price) as value, year, month " +
            "FROM expense " +
            "WHERE (year * 100 + month) >= (:startYear * 100 + :startMonth) " +
            "  AND (year * 100 + month) < (:endYear * 100 + :endMonth) " +
            "GROUP BY year, month " +
            "ORDER BY year DESC, month DESC")
    fun getPriceSumAfterAndBefore(startYear: Int, startMonth: Int, endYear: Int, endMonth: Int): Flow<List<BarChartEntry>>

    @Query("SELECT * " +
            "FROM expense " +
            "WHERE year=:year AND month=:month")
    fun getExpensesOfMonth(year: Int, month: Int): Flow<List<Expense>>

    @Query("SELECT * " +
            "FROM expense " +
            "WHERE year=:year")
    fun getExpensesOfYear(year: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE id = :id")
    override suspend fun getById(id: String): Expense?

    @Query("SELECT * FROM expense")
    override fun getAllSync(): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun upsert(item: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertAll(vararg items: Expense)

    @Update
    fun updateExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expense WHERE id = :id")
    override fun deleteById(id: String)

    @Query("DELETE FROM expense")
    fun deleteAll()

    @Transaction
    fun updateTable(vararg expenses: Expense) {
        deleteAll()
        insertAll(*expenses)
    }
}