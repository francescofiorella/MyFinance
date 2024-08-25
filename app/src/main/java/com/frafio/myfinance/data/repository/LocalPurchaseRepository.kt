package com.frafio.myfinance.data.repository

import androidx.lifecycle.LiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.model.BarChartEntry
import com.frafio.myfinance.data.model.Purchase
import com.frafio.myfinance.data.storage.MyFinanceDatabase

class LocalPurchaseRepository {
    private val purchaseDao =
        MyFinanceDatabase.getDatabase(MyFinanceApplication.instance).purchaseDao()

    fun getAll(): LiveData<List<Purchase>> = purchaseDao.getAll()

    fun getCount(): LiveData<Int> = purchaseDao.getCount()

    fun getPriceSumFromDay(year: Int, month: Int, day: Int): LiveData<Double?> =
        purchaseDao.getPriceSumOfDay(year, month, day)

    fun getPriceSumFromMonth(year: Int, month: Int): LiveData<Double?> =
        purchaseDao.getPriceSumOfMonth(year, month)

    fun getPriceSumFromYear(year: Int): LiveData<Double?> =
        purchaseDao.getPriceSumOfYear(year)

    fun getPriceSumAfterAndBefore(
        firstTimestamp: Long,
        lastTimestamp: Long
    ): LiveData<List<BarChartEntry>> =
        purchaseDao.getPriceSumAfterAndBefore(firstTimestamp, lastTimestamp)

    fun getPurchasesOfMonth(year: Int, month: Int): LiveData<List<Purchase>> =
        purchaseDao.getPurchasesOfMonth(year, month)

    fun getPurchasesOfYear(year: Int): LiveData<List<Purchase>> =
        purchaseDao.getPurchasesOfYear(year)

    fun insertPurchase(purchase: Purchase) = purchaseDao.insertPurchase(purchase)

    fun updatePurchase(purchase: Purchase) = purchaseDao.updatePurchase(purchase)

    fun deleteAll() = purchaseDao.deleteAll()

    fun deletePurchase(purchase: Purchase) = purchaseDao.deletePurchase(purchase)

    fun updateTable(purchases: List<Purchase>) = purchaseDao.updateTable(*purchases.toTypedArray())
}