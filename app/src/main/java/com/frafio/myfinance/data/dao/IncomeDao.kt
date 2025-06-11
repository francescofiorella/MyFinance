package com.frafio.myfinance.data.dao

import androidx.lifecycle.LiveData
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
    fun getAll(): LiveData<List<Income>>

    @Query("SELECT SUM(price) " +
            "FROM income " +
            "WHERE year=:year")
    fun getPriceSumOfYear(year: Int): LiveData<Double?>

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