package com.frafio.myfinance.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.frafio.myfinance.data.models.BarChartEntry
import com.frafio.myfinance.data.models.Purchase

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchase ORDER BY year DESC, month DESC, day DESC, price DESC, category DESC")
    fun getAll(): LiveData<List<Purchase>>

    @Query("SELECT COUNT(*) FROM purchase")
    fun getCount(): LiveData<Int>

    @Query("SELECT SUM(price) FROM purchase WHERE year=:year AND month=:month AND day=:day")
    fun getPriceSumOfDay(year: Int, month: Int, day: Int): LiveData<Double?>

    @Query("SELECT SUM(price) FROM purchase WHERE year=:year AND month=:month")
    fun getPriceSumOfMonth(year: Int, month: Int): LiveData<Double?>

    @Query("SELECT SUM(price) FROM purchase WHERE year=:year")
    fun getPriceSumOfYear(year: Int): LiveData<Double?>

    @Query("SELECT SUM(price) as value, year, month FROM purchase WHERE timestamp>=:firstTimestamp AND timestamp<:lastTimestamp GROUP BY year, month ORDER BY year DESC, month DESC")
    fun getPriceSumAfterAndBefore(firstTimestamp: Long, lastTimestamp: Long): LiveData<List<BarChartEntry>>

    @Query("SELECT * FROM purchase WHERE year=:year AND month=:month")
    fun getPurchasesOfMonth(year: Int, month: Int): LiveData<List<Purchase>>

    @Insert
    fun insertPurchase(purchase: Purchase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg purchases: Purchase)

    @Update
    fun updatePurchase(purchase: Purchase)

    @Delete
    fun deletePurchase(purchase: Purchase)

    @Query("DELETE FROM purchase")
    fun deleteAll()

    @Transaction
    fun updateTable(vararg purchases: Purchase) {
        deleteAll()
        insertAll(*purchases)
    }
}