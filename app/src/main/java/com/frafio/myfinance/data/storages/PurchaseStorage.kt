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
                    id = purchase.getTotalId(),
                    category = purchase.category
                )
            } else if (total!!.id == purchase.getTotalId()) { // If the total should be updated
                currentPurchases.add(purchase)
                total = Purchase(
                    email = UserStorage.user!!.email,
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = total!!.price!! + purchase.price!!,
                    year = total!!.year,
                    month = total!!.month,
                    day = total!!.day,
                    type = DbPurchases.TYPES.TOTAL.value,
                    id = total!!.getTotalId(),
                    category = total!!.category
                )
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
                        val totId = "${prevDate.dayOfMonth}_${prevDate.monthValue}_${prevDate.year}"
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
                total = Purchase(
                    email = UserStorage.user!!.email,
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = purchase.price,
                    year = purchase.year,
                    month = purchase.month,
                    day = purchase.day,
                    type = DbPurchases.TYPES.TOTAL.value,
                    id = purchase.getTotalId(),
                    category = purchase.category
                )
            }
        }
    }

    fun addPurchase(purchase: Purchase) {
        val purchaseDate = LocalDate.of(purchase.year!!, purchase.month!!, purchase.day!!)
        var i = 0
        var iDate =
            LocalDate.of(purchaseList[i].year!!, purchaseList[i].month!!, purchaseList[i].day!!)
        while (i < purchaseList.size && ChronoUnit.DAYS.between(purchaseDate, iDate) > 0) {
            i++
            iDate =
                LocalDate.of(purchaseList[i].year!!, purchaseList[i].month!!, purchaseList[i].day!!)
        }
        if (purchaseList[i].getDateString() == purchase.getDateString()) {
            // Totale trovato
            val total = Purchase(
                email = UserStorage.user!!.email,
                name = DbPurchases.NAMES.TOTAL.value,
                price = purchaseList[i].price!! + purchase.price!!,
                year = purchaseList[i].year,
                month = purchaseList[i].month,
                day = purchaseList[i].day,
                type = DbPurchases.TYPES.TOTAL.value,
                id = purchaseList[i].getTotalId(),
                category = purchaseList[i].category
            )
            purchaseList[i] = total
            // Scorri per trovare posizione giusta
            i++
            while (i < purchaseList.size) {
                if (purchaseList[i].getDateString() != purchase.getDateString() ||
                    purchaseList[i].price!! < purchase.price
                ) {
                    break
                }
                i++
            }
            purchaseList.add(i, purchase)
        } else {
            // Giorno non esistente, aggiungi totale
            val total = Purchase(
                email = UserStorage.user!!.email,
                name = DbPurchases.NAMES.TOTAL.value,
                price = purchase.price,
                year = purchase.year,
                month = purchase.month,
                day = purchase.day,
                type = DbPurchases.TYPES.TOTAL.value,
                id = purchase.getTotalId(),
                category = purchase.category
            )
            purchaseList.add(i, total)
            purchaseList.add(i + 1, purchase)
        }
    }

    fun deletePurchaseAt(position: Int) {
        for (i in position - 1 downTo 0) {
            if (purchaseList[i].type == DbPurchases.TYPES.TOTAL.value) {
                val newTotal = Purchase(
                    email = UserStorage.user!!.email,
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = purchaseList[i].price!! - purchaseList[position].price!!,
                    year = purchaseList[i].year,
                    month = purchaseList[i].month,
                    day = purchaseList[i].day,
                    type = DbPurchases.TYPES.TOTAL.value,
                    id = purchaseList[i].getTotalId(),
                    category = purchaseList[i].category
                )

                purchaseList.removeAt(position)
                val todayDate = LocalDate.now()
                val totalDate = LocalDate.of(
                    newTotal.year!!,
                    newTotal.month!!,
                    newTotal.day!!
                )
                if (ChronoUnit.DAYS.between(totalDate, todayDate) >= 0) {
                    purchaseList[i] = newTotal
                } else if (newTotal.price == 0.0) {
                    purchaseList.removeAt(i)
                }

                break
            }
        }
    }

    fun editPurchaseAt(position: Int, purchase: Purchase) {
        val previous = purchaseList[position]
        purchaseList[position] = purchase
        if (previous.price == purchase.price) {
            return
        } else {
            var i = position - 1
            while (i >= 0) {
                if (purchaseList[i].type == DbPurchases.TYPES.TOTAL.value) {
                    val total = Purchase(
                        email = purchaseList[i].email,
                        name = purchaseList[i].name,
                        price = purchaseList[i].price!! - previous.price!! + purchase.price!!,
                        year = purchaseList[i].year,
                        month = purchaseList[i].month,
                        day = purchaseList[i].day,
                        type = purchaseList[i].type,
                        id = purchaseList[i].id,
                        category = purchaseList[i].category
                    )
                    purchaseList[i] = total
                    break
                }
                i--
            }
        }
    }
}