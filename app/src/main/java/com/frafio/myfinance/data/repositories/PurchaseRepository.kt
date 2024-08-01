package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult

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

    fun getMonthlyBudget(): LiveData<PurchaseResult> {
        return purchaseManager.getMonthlyBudget()
    }

    fun setMonthlyBudget(budget: Double): LiveData<PurchaseResult> {
        return purchaseManager.setMonthlyBudget(budget)
    }

    fun updateLocalMonthlyBudget() {
        purchaseManager.updateLocalMonthlyBudget()
    }
}