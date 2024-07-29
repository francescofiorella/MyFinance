package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage

class PurchaseRepository(private val purchaseManager: PurchaseManager) {
    fun updatePurchaseList(): LiveData<PurchaseResult> {
        return purchaseManager.updatePurchaseList()
    }

    fun getPurchaseNumber(
        collection: String = DbPurchases.FIELDS.PAYMENTS.value
    ): LiveData<PurchaseResult> {
        return purchaseManager.getPurchaseNumber(collection)
    }

    fun deletePurchase(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.deletePurchase(purchase)
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.addPurchase(purchase)
    }

    fun editPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        return purchaseManager.editPurchase(purchase)
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