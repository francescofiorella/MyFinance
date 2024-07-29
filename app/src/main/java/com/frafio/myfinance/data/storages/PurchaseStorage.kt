package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Purchase
import java.time.LocalDate

object PurchaseStorage {
    var monthlyBudget: Double = 0.0
    var isTableBusy = false

    fun resetBudget() {
        monthlyBudget = 0.0
    }

    fun addTotals(purchases: List<Purchase>): List<Purchase> {
        val purchaseList: MutableList<Purchase> = mutableListOf()
        // Create total for the local list
        var total: Purchase? = null
        // Used to keep the order
        var currentPurchases = mutableListOf<Purchase>()

        var prevDate: LocalDate? = null

        purchases.forEach { purchase ->
            val todayDate = LocalDate.now()
            val purchaseDate = purchase.getLocalDate()
            total?.let {
                prevDate = total!!.getLocalDate()
            }

            if ((prevDate == null || prevDate!!.isAfter(todayDate)) &&
                purchaseDate.isBefore(todayDate)
            ) {
                if (currentPurchases.isNotEmpty()) {
                    purchaseList.add(total!!)
                    currentPurchases.forEach { p ->
                        purchaseList.add(p)
                    }
                }
                currentPurchases = mutableListOf()
                // Aggiungi totale a 0 per oggi
                val totId = "${todayDate.dayOfMonth}_${todayDate.monthValue}_${todayDate.year}"
                total = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = 0.0,
                    year = todayDate.year,
                    month = todayDate.monthValue,
                    day = todayDate.dayOfMonth,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = totId
                )
                purchaseList.add(total!!)
                prevDate = total!!.getLocalDate()
            }

            if (prevDate == null) { // If is the first total
                currentPurchases.add(purchase)
                total = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = purchase.price,
                    year = purchase.year,
                    month = purchase.month,
                    day = purchase.day,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = purchase.getTotalId()
                )
            } else if (total!!.id == purchase.getTotalId()) { // If the total should be updated
                currentPurchases.add(purchase)
                total = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = total!!.price!! + purchase.price!!,
                    year = total!!.year,
                    month = total!!.month,
                    day = total!!.day,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = total!!.getTotalId()
                )
            } else { // If we need a new total
                // Update the local list with previous day purchases
                if (currentPurchases.isNotEmpty()) {
                    purchaseList.add(total!!)
                    currentPurchases.forEach { p ->
                        purchaseList.add(p)
                    }
                }
                currentPurchases = mutableListOf()
                currentPurchases.add(purchase)
                // Create new total
                total = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = purchase.price,
                    year = purchase.year,
                    month = purchase.month,
                    day = purchase.day,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = purchase.getTotalId()
                )
            }
        }
        if (currentPurchases.isNotEmpty()) {
            purchaseList.add(total!!)
            currentPurchases.forEach { p ->
                purchaseList.add(p)
            }
        }
        return purchaseList
    }
}