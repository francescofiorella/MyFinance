package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Purchase
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate

object PurchaseStorage {
    var purchaseList: MutableList<Purchase> = mutableListOf()
    var incomeList: MutableList<Purchase> = mutableListOf()
    var monthlyBudget: Double = 0.0

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }

    fun resetIncomeList() {
        incomeList = mutableListOf()
    }

    fun resetBudget() {
        monthlyBudget = 0.0
    }

    fun populatePaymentsFromSnapshot(queryDocumentSnapshots: QuerySnapshot) {
        resetPurchaseList()
        // Create total for the local list
        var total: Purchase? = null
        // Used to keep the order
        var currentPurchases = mutableListOf<Purchase>()

        var prevDate: LocalDate? = null

        queryDocumentSnapshots.forEach { document ->
            val purchase = document.toObject(Purchase::class.java)
            // set id
            purchase.id = document.id
            val todayDate = LocalDate.now()
            val purchaseDate = purchase.getLocalDate()
            total?.let {
                prevDate = total!!.getLocalDate()
            }

            if ((prevDate == null || prevDate!!.isAfter(todayDate)) &&
                purchaseDate.isBefore(todayDate)
            ) {
                addToListFrom(false, currentPurchases, total)
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
                addToListFrom(false, currentPurchases, total)
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
        addToListFrom(false, currentPurchases, total)
    }

    private fun addToListFrom(income: Boolean = false, list: List<Purchase>, total: Purchase?) {
        // income is True for purchaseList, False for incomeList
        total?.let {
            if (list.isNotEmpty()) {
                if (income) incomeList.add(total) else purchaseList.add(total)
                list.forEach { p ->
                    if (income) incomeList.add(p) else purchaseList.add(p)
                }
            }
        }
    }

    fun addPurchase(purchase: Purchase): Int {
        val totalIndex: Int
        val purchaseDate = purchase.getLocalDate()
        var i = 0
        val totalFound = purchaseList.any { it.getDateString() == purchase.getDateString() }
        for (p in purchaseList) {
            if (purchaseDate.isBefore(p.getLocalDate())) {
                i++
            } else {
                break
            }
        }

        if (totalFound) {
            // Aggiorna totale
            val total = Purchase(
                name = DbPurchases.NAMES.TOTAL.value,
                price = purchaseList[i].price!! + purchase.price!!,
                year = purchaseList[i].year,
                month = purchaseList[i].month,
                day = purchaseList[i].day,
                category = DbPurchases.CATEGORIES.TOTAL.value,
                id = purchaseList[i].getTotalId()
            )
            purchaseList[i] = total
            totalIndex = i
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
                name = DbPurchases.NAMES.TOTAL.value,
                price = purchase.price,
                year = purchase.year,
                month = purchase.month,
                day = purchase.day,
                category = DbPurchases.CATEGORIES.TOTAL.value,
                id = purchase.getTotalId()
            )
            purchaseList.add(i, total)
            totalIndex = i
            purchaseList.add(i + 1, purchase)
        }
        return totalIndex
    }

    fun deletePurchaseAt(position: Int) {
        val todayDate = LocalDate.now()
        for (i in position - 1 downTo 0) {
            if (purchaseList[i].category == DbPurchases.CATEGORIES.TOTAL.value) {
                val newTotal = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = purchaseList[i].price!! - purchaseList[position].price!!,
                    year = purchaseList[i].year,
                    month = purchaseList[i].month,
                    day = purchaseList[i].day,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = purchaseList[i].getTotalId()
                )

                purchaseList.removeAt(position)
                if (
                    incomeList.size > 1
                    && newTotal.day == todayDate.dayOfMonth
                    && newTotal.month == todayDate.monthValue
                    && newTotal.year == todayDate.year
                    || newTotal.price != 0.0
                ) {
                    purchaseList[i] = newTotal
                } else {
                    purchaseList.removeAt(i)
                }

                break
            }
        }
    }

    fun populateIncomesFromSnapshot(queryDocumentSnapshots: QuerySnapshot) {
        resetIncomeList()

        val todayYear = LocalDate.now().year
        var total = Purchase(
            name = DbPurchases.NAMES.TOTAL.value,
            price = 0.0,
            year = todayYear,
            month = 0,
            day = 0,
            category = DbPurchases.CATEGORIES.TOTAL.value,
            id = todayYear.toString()
        )
        var currentIncomes = mutableListOf<Purchase>()

        var isFirstIncome = true
        queryDocumentSnapshots.forEach { document ->
            val income = document.toObject(Purchase::class.java)
            // set id
            income.id = document.id

            if (isFirstIncome && todayYear > income.year!!) {
                // Inserisci totale a 0.0 per oggi
                incomeList.add(
                    total
                )
                total = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = 0.0,
                    year = income.year,
                    month = 0,
                    day = 0,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = income.year.toString()
                )
            } else if (isFirstIncome && income.year!! > todayYear) {
                total = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = 0.0,
                    year = income.year,
                    month = 0,
                    day = 0,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = income.year.toString()
                )
            }

            isFirstIncome = false

            // Popola lista
            if (total.year!! == income.year) { // Se totale corrisponde, aggiorna
                total = Purchase(
                    name = total.name,
                    price = total.price!! + income.price!!,
                    year = total.year,
                    month = total.month,
                    day = total.day,
                    category = total.category,
                    id = total.id
                )
                currentIncomes.add(income)
            } else { // Crea nuovo totale
                addToListFrom(true, currentIncomes, total)
                currentIncomes = mutableListOf()
                total = Purchase(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = income.price,
                    year = income.year,
                    month = 0,
                    day = 0,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = income.year.toString()
                )
                currentIncomes.add(income)
            }
        }
        addToListFrom(true, currentIncomes, total)
    }

    fun addIncome(income: Purchase): Int {
        val totalIndex: Int
        var i = 0
        val totalFound = incomeList.any { it.year == income.year }
        for (p in incomeList) {
            if (incomeList[i].year!! > income.year!!) {
                i++
            } else {
                break
            }
        }

        if (totalFound) {
            // Aggiorna totale
            val total = Purchase(
                name = DbPurchases.NAMES.TOTAL.value,
                price = incomeList[i].price!! + income.price!!,
                year = incomeList[i].year,
                month = incomeList[i].month,
                day = incomeList[i].day,
                category = DbPurchases.CATEGORIES.TOTAL.value,
                id = incomeList[i].getTotalId()
            )
            incomeList[i] = total
            totalIndex = i
            // Scorri per trovare posizione giusta
            i++
            while (i < incomeList.size) {
                if (incomeList[i].year != income.year
                    || incomeList[i].month!! < income.month!!
                    || (incomeList[i].month == income.month
                            && incomeList[i].day!! < income.day!!)
                    || (incomeList[i].month == income.month
                            && incomeList[i].day == income.day
                            && incomeList[i].price!! < income.price)
                ) {
                    break
                }
                i++
            }
            incomeList.add(i, income)
        } else {
            // Giorno non esistente, aggiungi totale
            val total = Purchase(
                name = DbPurchases.NAMES.TOTAL.value,
                price = income.price,
                year = income.year,
                month = income.month,
                day = income.day,
                category = DbPurchases.CATEGORIES.TOTAL.value,
                id = income.getTotalId()
            )
            incomeList.add(i, total)
            totalIndex = i
            incomeList.add(i + 1, income)
        }
        return totalIndex
    }

    fun deleteIncomeAt(position: Int) {
        val todayYear = LocalDate.now().year
        for (i in position - 1 downTo 0) {
            if (incomeList[i].category == DbPurchases.CATEGORIES.TOTAL.value) {
                val newTotal = Purchase(
                    name = incomeList[i].name,
                    price = incomeList[i].price!! - incomeList[position].price!!,
                    year = incomeList[i].year,
                    month = incomeList[i].month,
                    day = incomeList[i].day,
                    category = incomeList[i].category,
                    id = incomeList[i].id
                )

                incomeList.removeAt(position)
                if (incomeList.size > 1 && newTotal.year == todayYear || newTotal.price != 0.0) {
                    incomeList[i] = newTotal
                } else {
                    incomeList.removeAt(i)
                }

                break
            }
        }
    }
}