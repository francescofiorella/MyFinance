package com.frafio.myfinance.data

import com.frafio.myfinance.data.db.entities.Purchase

class PurchaseRepository private constructor(private val purchaseDao: PurchaseDao) : PurchaseInterfaceRepository{
    override fun add(purchase: Purchase) {
        purchaseDao.add(purchase)
    }

    override fun getAll() = purchaseDao.getAll()

    override fun delete(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    override fun update(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    companion object {
        @Volatile private var istance: PurchaseRepository? = null

        fun getInstance(purchaseDao: PurchaseDao) =
            istance ?: synchronized(this) {
                istance ?: PurchaseRepository(purchaseDao).also { istance = it }
            }
    }
}