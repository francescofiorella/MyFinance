package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Purchase
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object PurchaseStorage {
    var purchaseList: MutableList<Purchase> = mutableListOf()

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }

    fun populateListFromSnapshot(queryDocumentSnapshots: QuerySnapshot) {
        resetPurchaseList()
        // Create total for the local list
        var total: Purchase? = null
        // Used to keep the order
        var currentPurchases = mutableListOf<Purchase>()

        queryDocumentSnapshots.forEach { document ->
            val purchase = document.toObject(Purchase::class.java)
            // set id
            purchase.updateID(document.id)

            var todayDate = LocalDate.now()
            val purchaseDate =
                LocalDate.of(purchase.year!!, purchase.month!!, purchase.day!!)
            var prevDate: LocalDate? = if (total == null)
                null
            else
                LocalDate.of(total!!.year!!, total!!.month!!, total!!.day!!)

            // se è < today and non hai fatto today
            // quindi se purchase < today and (totale == null or
            // totale > today)
            if (prevDate == null &&
                ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0
            ) {
                // Aggiungi totali a 0 per ogni giorno tra oggi e purchase
                val totToAdd = ChronoUnit.DAYS.between(purchaseDate, todayDate)
                for (i in 0..<totToAdd) {
                    val totId =
                        "${todayDate.dayOfMonth}_${todayDate.monthValue}_${todayDate.year}"
                    total = Purchase(
                        email = UserStorage.user!!.email,
                        name = DbPurchases.NAMES.TOTAL.value,
                        price = 0.0,
                        year = todayDate.year,
                        month = todayDate.monthValue,
                        day = todayDate.dayOfMonth,
                        type = DbPurchases.TYPES.TOTAL.value,
                        id = totId,
                        category = purchase.category
                    )
                    purchaseList.add(total!!)
                    prevDate =
                        LocalDate.of(
                            total!!.year!!,
                            total!!.month!!,
                            total!!.day!!
                        )
                    todayDate = todayDate.minusDays(1)
                }
                todayDate = LocalDate.now()
            }

            var totId = "${purchase.day}_${purchase.month}_${purchase.year}"
            if (prevDate == null) { // If is the first total
                currentPurchases.add(purchase)
                total = Purchase(
                    email = UserStorage.user!!.email,
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = purchase.price,
                    year = purchase.year,
                    month = purchase.month,
                    day = purchase.day,
                    type = DbPurchases.TYPES.TOTAL.value,
                    id = totId,
                    category = purchase.category
                )
            } else if (total!!.id == totId) { // If the total should be updated
                currentPurchases.add(purchase)
                total!!.price = total!!.price!!.plus(purchase.price ?: 0.0)
            } else { // If we need a new total
                // Update the local list with previous day purchases
                if (currentPurchases.isNotEmpty()) {
                    purchaseList.add(total!!)
                    currentPurchases.forEach { cPurchase ->
                        purchaseList.add(cPurchase)
                    }
                }
                // aggiungi 0 anche se totale - purchase > 1,
                // aggiungi uno 0 per ogni differenza tra totale e purchase
                val startFromToday =
                    ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0 &&
                            ChronoUnit.DAYS.between(todayDate, prevDate) > 0
                val totToAdd = if (startFromToday)
                    ChronoUnit.DAYS.between(purchaseDate, todayDate) + 1
                else
                    ChronoUnit.DAYS.between(purchaseDate, prevDate)
                if (ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0 &&
                    totToAdd > 1
                ) {
                    if (startFromToday) {
                        prevDate = LocalDate.now().plusDays(1)
                    }
                    for (i in 1..<totToAdd) {
                        prevDate = prevDate!!.minusDays(1)
                        totId =
                            "${prevDate.dayOfMonth}_${prevDate.monthValue}_${prevDate.year}"
                        total = Purchase(
                            email = UserStorage.user!!.email,
                            name = DbPurchases.NAMES.TOTAL.value,
                            price = 0.0,
                            year = prevDate.year,
                            month = prevDate.monthValue,
                            day = prevDate.dayOfMonth,
                            type = DbPurchases.TYPES.TOTAL.value,
                            id = totId,
                            category = purchase.category
                        )
                        purchaseList.add(total!!)
                    }
                }

                // Create new total
                currentPurchases = mutableListOf()
                currentPurchases.add(purchase)
                totId = "${purchase.day}_${purchase.month}_${purchase.year}"
                total = Purchase(
                    email = UserStorage.user!!.email,
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = purchase.price,
                    year = purchase.year,
                    month = purchase.month,
                    day = purchase.day,
                    type = DbPurchases.TYPES.TOTAL.value,
                    id = totId,
                    category = purchase.category
                )
            }
        }
    }
}