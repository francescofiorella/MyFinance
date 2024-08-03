package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.models.BarChartEntry
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.storages.MyFinanceDatabase

class LocalPurchaseRepository {
    private val purchaseDao =
        MyFinanceDatabase.getDatabase(MyFinanceApplication.instance).purchaseDao()

    fun getAll(): LiveData<List<Purchase>> = purchaseDao.getAll()

    fun getCount(): LiveData<Int> = purchaseDao.getCount()

    fun getPriceSumFromDay(year: Int, month: Int, day: Int): LiveData<Double?> =
        purchaseDao.getPriceSumFromDay(year, month, day)

    fun getPriceSumFromMonth(year: Int, month: Int): LiveData<Double?> =
        purchaseDao.getPriceSumFromMonth(year, month)

    fun getPriceSumFromYear(year: Int): LiveData<Double?> =
        purchaseDao.getPriceSumFromYear(year)

    fun getPurchasesAfterAndBefore(firstTimestamp: Long, lastTimestamp: Long): LiveData<List<BarChartEntry>> =
        purchaseDao.getAfterAndBefore(firstTimestamp, lastTimestamp)

    fun insertPurchase(purchase: Purchase) = purchaseDao.insertPurchase(purchase)

    fun updatePurchase(purchase: Purchase) = purchaseDao.updatePurchase(purchase)

    fun deleteAll() = purchaseDao.deleteAll()

    fun deletePurchase(purchase: Purchase) = purchaseDao.deletePurchase(purchase)

    fun updateTable(purchases: List<Purchase>) = purchaseDao.updateTable(*purchases.toTypedArray())
}