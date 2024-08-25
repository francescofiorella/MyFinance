package com.frafio.myfinance.data.storage

import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.model.Income
import java.time.LocalDate

object IncomeStorage {
    fun addTotals(incomes: List<Income>): List<Income> {
        val incomeList = mutableListOf<Income>()

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
        incomes.forEach { income ->
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
                if (currentIncomes.isNotEmpty()) {
                    incomeList.add(total)
                    currentIncomes.forEach { p ->
                        incomeList.add(p)
                    }
                }
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
        if (currentIncomes.isNotEmpty()) {
            incomeList.add(total)
            currentIncomes.forEach { p ->
                incomeList.add(p)
            }
        }
        return incomeList
    }
}