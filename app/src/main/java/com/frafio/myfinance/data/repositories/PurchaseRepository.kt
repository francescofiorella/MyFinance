package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage

class PurchaseRepository(private val purchaseManager: PurchaseManager) {

    fun getIncomeList(): List<Purchase> {
        return PurchaseStorage.incomeList
    }

    fun getPurchaseList(): List<Purchase> {
        return PurchaseStorage.purchaseList
    }

    fun updatePurchaseList(limit: Long): LiveData<PurchaseResult> {
        return purchaseManager.updatePurchaseList(limit)
    }

    fun updateIncomeList(limit: Long): LiveData<PurchaseResult> {
        return purchaseManager.updateIncomeList(limit)
    }

    fun getPurchaseNumber(
        collection: String = DbPurchases.FIELDS.PAYMENTS.value
    ): LiveData<PurchaseResult> {
        return purchaseManager.getPurchaseNumber(collection)
    }

    fun getThisYearTotal(
        result: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        return purchaseManager.getThisYearTotal(result)
    }

    fun getTodayTotal(
        result: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        return purchaseManager.getTodayTotal(result)
    }

    fun getThisMonthTotal(
        result: MutableLiveData<Pair<PurchaseCode, Double>> = MutableLiveData()
    ): MutableLiveData<Pair<PurchaseCode, Double>> {
        return purchaseManager.getThisMonthTotal(result)
    }

    fun deletePurchaseAt(position: Int): LiveData<PurchaseResult> {
        return purchaseManager.deletePurchaseAt(position)
    }

    fun deleteIncomeAt(position: Int): LiveData<PurchaseResult> {
        return purchaseManager.deleteIncomeAt(position)
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.addPurchase(purchase)
    }

    fun addIncome(income: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.addIncome(income)
    }

    fun editPurchase(
        purchase: Purchase,
        position: Int
    ): LiveData<PurchaseResult> {
        return purchaseManager.editPurchase(purchase, position)
    }

    fun editIncome(
        income: Purchase,
        position: Int
    ): LiveData<PurchaseResult> {
        return purchaseManager.editIncome(income, position)
    }

    fun setDynamicColorActive(active: Boolean) {
        purchaseManager.setDynamicColorActive(active)
    }

    fun getDynamicColorActive(): Boolean {
        return purchaseManager.getDynamicColorActive()
    }

    fun getMonthlyBudgetFromStorage(): Double {
        return PurchaseStorage.monthlyBudget
    }

    fun getMonthlyBudget(): LiveData<PurchaseResult> {
        return purchaseManager.getMonthlyBudget()
    }

    fun updateMonthlyBudget(budget: Double): LiveData<PurchaseResult> {
        return purchaseManager.updateMonthlyBudget(budget)
    }

    fun getLastYearPurchases(
        result: MutableLiveData<List<Purchase>> = MutableLiveData()
    ): LiveData<List<Purchase>> {
        return purchaseManager.getLastYearPurchases(result)
    }
}