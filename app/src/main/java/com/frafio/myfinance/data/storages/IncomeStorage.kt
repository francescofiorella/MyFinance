package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Income
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate

object IncomeStorage {
    var incomeList: MutableList<Income> = mutableListOf()

    fun resetIncomeList() {
        incomeList = mutableListOf()
    }

    private fun addToListFrom(list: List<Income>, total: Income?) {
        // income is True for purchaseList, False for incomeList
        total?.let {
            if (list.isNotEmpty()) {
                incomeList.add(total)
                list.forEach { i ->
                    incomeList.add(i)
                }
            }
        }
    }

    fun populateIncomesFromSnapshot(queryDocumentSnapshots: QuerySnapshot) {
        resetIncomeList()

        val todayYear = LocalDate.now().year
        var total = Income(
            name = DbPurchases.NAMES.TOTAL.value,
            price = 0.0,
            year = todayYear,
            month = 0,
            day = 0,
            category = DbPurchases.CATEGORIES.TOTAL.value,
            id = todayYear.toString()
        )
        var currentIncomes = mutableListOf<Income>()

        var isFirstIncome = true
        queryDocumentSnapshots.forEach { document ->
            val income = document.toObject(Income::class.java)
            // set id
            income.id = document.id

            if (isFirstIncome && todayYear > income.year!!) {
                // Inserisci totale a 0.0 per oggi
                incomeList.add(
                    total
                )
                total = Income(
                    name = DbPurchases.NAMES.TOTAL.value,
                    price = 0.0,
                    year = income.year,
                    month = 0,
                    day = 0,
                    category = DbPurchases.CATEGORIES.TOTAL.value,
                    id = income.year.toString()
                )
            } else if (isFirstIncome && income.year!! > todayYear) {
                total = Income(
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
                total = Income(
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
                addToListFrom(currentIncomes, total)
                currentIncomes = mutableListOf()
                total = Income(
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
        addToListFrom(currentIncomes, total)
    }

    fun addIncome(income: Income): Int {
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
            val total = Income(
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
            val total = Income(
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
                val newTotal = Income(
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