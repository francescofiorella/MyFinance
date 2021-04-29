package com.frafio.myfinance.data

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.db.entities.Purchase

interface PurchaseInterfaceDao {
     fun add(purchase: Purchase)

     fun getAll(): LiveData<List<Purchase>>

     fun update(purchase: Purchase)

     fun delete(purchase: Purchase)
}