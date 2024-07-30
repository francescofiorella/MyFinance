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
    fun getPriceSumFromDay(year: Int, month: Int, day: Int): LiveData<Double?>

    @Query("SELECT SUM(price) FROM purchase WHERE year=:year AND month=:month")
    fun getPriceSumFromMonth(year: Int, month: Int): LiveData<Double?>

    @Query("SELECT SUM(price) FROM purchase WHERE year=:year")
    fun getPriceSumFromYear(year: Int): LiveData<Double?>

    @Query("SELECT SUM(price) as value, year, month FROM purchase WHERE timestamp>=:timestamp GROUP BY year, month ORDER BY year DESC, month DESC")
    fun getAfter(timestamp: Long): LiveData<List<BarChartEntry>>

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