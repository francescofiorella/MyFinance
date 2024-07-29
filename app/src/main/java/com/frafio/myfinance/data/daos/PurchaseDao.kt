package com.frafio.myfinance.data.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query(
        "WITH RECURSIVE MonthRange AS (\n" +
                "    SELECT\n" +
                "        :startYear AS year,\n" +
                "        :startMonth AS month\n" +
                "    UNION ALL\n" +
                "    SELECT\n" +
                "        CASE\n" +
                "            WHEN month < 12 THEN year\n" +
                "            ELSE year + 1\n" +
                "        END,\n" +
                "        CASE\n" +
                "            WHEN month < 12 THEN month + 1\n" +
                "            ELSE 1\n" +
                "        END\n" +
                "    FROM MonthRange\n" +
                "    WHERE NOT (year = :endYear AND month = :endMonth)\n" +
                ")\n" +
                "SELECT\n" +
                "    COALESCE(SUM(Purchase.price), 0) AS total_price\n" +
                "FROM\n" +
                "    MonthRange\n" +
                "LEFT JOIN\n" +
                "    Purchase ON Purchase.year = MonthRange.year AND Purchase.month = MonthRange.month\n" +
                "GROUP BY\n" +
                "    MonthRange.year,\n" +
                "    MonthRange.month\n" +
                "ORDER BY\n" +
                "    MonthRange.year,\n" +
                "    MonthRange.month;"
    )
    fun getPricesPerInterval(
        startYear: Int,
        endYear: Int,
        startMonth: Int,
        endMonth: Int
    ): LiveData<List<Double?>>

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
}