package com.frafio.myfinance.data

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.db.entities.Purchase

interface PurchaseInterfaceRepository {
    fun add(purchase: Purchase)

    fun getAll(): LiveData<List<Purchase>>

    fun delete(purchase: Purchase)

    fun update(purchase: Purchase)
}