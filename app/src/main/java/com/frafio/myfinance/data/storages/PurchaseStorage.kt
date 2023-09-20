package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Purchase

object PurchaseStorage {
    var purchaseList: MutableList<Purchase> = mutableListOf()

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }
}