package com.frafio.myfinance.data.storage

import com.frafio.myfinance.data.models.Purchase

object PurchaseStorage {

    private var purchaseList: MutableList<Purchase> = mutableListOf()

    fun updatePurchaseAt(index: Int, purchase: Purchase) {
        purchaseList[index] = purchase
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