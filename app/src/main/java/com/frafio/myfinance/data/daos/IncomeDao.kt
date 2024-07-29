package com.frafio.myfinance.data.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.frafio.myfinance.data.models.Income

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income ORDER BY timestamp, price DESC")
    fun getAll(): List<Income>

    @Insert
    fun addIncome(income: Income)

    @Update
    fun updateIncome(income: Income)

    @Delete
    fun deleteIncome(income: Income)
}