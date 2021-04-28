package com.frafio.myfinance.data

import androidx.lifecycle.LiveData

interface PurchaseInterfaceRepository {
    fun add(purchase: Purchase)

    fun getAll(): LiveData<List<Purchase>>

    fun delete(purchase: Purchase)

    fun update(purchase: Purchase)
}