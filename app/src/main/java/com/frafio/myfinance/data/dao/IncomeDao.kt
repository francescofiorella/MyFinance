package com.frafio.myfinance.data.dao

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.frafio.myfinance.data.model.Income

@Dao
interface IncomeDao {
    @Query("SELECT * " +
            "FROM income " +
            "ORDER BY year DESC, month DESC, day DESC, price DESC")
    fun getAll(): Flow<List<Income>>

    @Query("SELECT COUNT(*) FROM income")
    fun getCount(): Flow<Int>

    @Query("SELECT SUM(price) " +
            "FROM income " +
            "WHERE year=:year")
    fun getPriceSumOfYear(year: Int): Flow<Double?>

    @Insert
    fun insertIncome(income: Income)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg incomes: Income)

    @Update
    fun updateIncome(income: Income)

    @Delete
    fun deleteIncome(income: Income)

    @Query("DELETE FROM income")
    fun deleteAll()

    @Transaction
    fun updateTable(vararg incomes: Income) {
        deleteAll()
        insertAll(*incomes)
    }
}