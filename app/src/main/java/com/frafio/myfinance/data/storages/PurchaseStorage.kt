package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.models.Purchase

object PurchaseStorage {
    var purchaseList: MutableList<Purchase> = mutableListOf()

    var existLastYear: Boolean = false

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }
}