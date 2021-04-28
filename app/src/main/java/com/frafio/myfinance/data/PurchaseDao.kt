package com.frafio.myfinance.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PurchaseDao : PurchaseInterfaceDao {
    private val purchaseList = mutableListOf<Purchase>()
    private val purchases = MutableLiveData<List<Purchase>>()

    init {
        purchases.value = purchaseList
    }

    override fun add(purchase: Purchase) {
        purchaseList.add(purchase)
        purchases.value = purchaseList
    }

    override fun getAll() = purchases as LiveData<List<Purchase>>

    override fun update(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    override fun delete(purchase: Purchase) {
        TODO("Not yet implemented")
    }
}