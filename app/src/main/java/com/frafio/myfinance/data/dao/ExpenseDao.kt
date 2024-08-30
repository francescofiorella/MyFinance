package com.frafio.myfinance.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.frafio.myfinance.data.model.BarChartEntry
import com.frafio.myfinance.data.model.Expense

@Dao
interface ExpenseDao {
    @Query("SELECT * " +
            "FROM expense " +
            "ORDER BY year DESC, month DESC, day DESC, price DESC, category DESC")
    fun getAll(): LiveData<List<Expense>>

    @Query("SELECT COUNT(*) " +
            "FROM expense")
    fun getCount(): LiveData<Int>

    @Query("SELECT SUM(price) " +
            "FROM expense " +
            "WHERE year=:year AND month=:month AND day=:day")
    fun getPriceSumOfDay(year: Int, month: Int, day: Int): LiveData<Double?>

    @Query("SELECT SUM(price) " +
            "FROM expense " +
            "WHERE year=:year AND month=:month")
    fun getPriceSumOfMonth(year: Int, month: Int): LiveData<Double?>

    @Query("SELECT SUM(price) " +
            "FROM expense " +
            "WHERE year=:year")
    fun getPriceSumOfYear(year: Int): LiveData<Double?>

    @Query("SELECT SUM(price) as value, year, month " +
            "FROM expense " +
            "WHERE timestamp>=:firstTimestamp AND timestamp<:lastTimestamp " +
            "GROUP BY year, month " +
            "ORDER BY year DESC, month DESC")
    fun getPriceSumAfterAndBefore(firstTimestamp: Long, lastTimestamp: Long): LiveData<List<BarChartEntry>>

    @Query("SELECT * " +
            "FROM expense " +
            "WHERE year=:year AND month=:month")
    fun getExpensesOfMonth(year: Int, month: Int): LiveData<List<Expense>>

    @Query("SELECT * " +
            "FROM expense " +
            "WHERE year=:year")
    fun getExpensesOfYear(year: Int): LiveData<List<Expense>>

    @Query("SELECT * " +
            "FROM expense " +
            "WHERE name LIKE :string || '%' OR name LIKE '% ' || :string || '%' " +
            "ORDER BY year DESC, month DESC, day DESC, price DESC, category DESC")
    fun getStartingWith(string: String): LiveData<List<Expense>>

    @Insert
    fun insertExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg expenses: Expense)

    @Update
    fun updateExpense(expense: Expense)

    @Delete
    fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expense")
    fun deleteAll()

    @Transaction
    fun updateTable(vararg expenses: Expense) {
        deleteAll()
        insertAll(*expenses)
    }
}