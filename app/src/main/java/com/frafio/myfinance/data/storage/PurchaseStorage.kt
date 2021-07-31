package com.frafio.myfinance.data.storage

import com.frafio.myfinance.data.models.Purchase

object PurchaseStorage {

    var purchaseList: MutableList<Purchase> = mutableListOf()

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }
}