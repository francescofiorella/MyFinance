package com.frafio.myfinance.data.manager

import com.frafio.myfinance.data.models.Purchase

object PurchaseManager {

    private var purchaseList: MutableList<Purchase> = mutableListOf()

    fun updatePurchaseAt(index: Int, purchase: Purchase) {
        purchaseList[index] = purchase
    }

    fun getPurchaseAt(index: Int): Purchase? {
        return if (!purchaseList.isNullOrEmpty()) {
            purchaseList[index]
        } else {
            null
        }
    }

    fun addPurchase(purchase: Purchase) {
        purchaseList.add(purchase)
    }

    fun setPurchaseList(list: MutableList<Purchase>) {
        purchaseList = list
    }

    fun getPurchaseList(): MutableList<Purchase> {
        return purchaseList
    }

    fun getPurchaseListSize(): Int {
        return purchaseList.size
    }

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }
}