package com.frafio.myfinance.core.utils

import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.MonthlyTotal
import java.time.LocalDate

fun getCurrencyIcon(currencyCode: String): Int {
    return when (currencyCode) {
        "EUR" -> R.drawable.ic_euro_filled
        "FRF" -> R.drawable.ic_currency_franc_filled
        "TRY" -> R.drawable.ic_currency_lira_filled
        "GBP", "FKP", "GIP", "SHP", "SSP" -> R.drawable.ic_currency_pound_filled
        "RUB" -> R.drawable.ic_currency_ruble_filled
        "JPY" -> R.drawable.ic_currency_yen_filled
        "CNY" -> R.drawable.ic_currency_yuan_filled
        else -> R.drawable.ic_attach_money_filled
    }
}

// Groups consecutive rows by date, so expenses must already be sorted newest-first as the DAO returns them.
fun addTotalsToExpenses(expenses: List<Expense>, today: LocalDate = LocalDate.now()): List<Expense> {
    if (expenses.isEmpty()) return emptyList()

    val result = ArrayList<Expense>(expenses.size + 10)
    var todayAdded = false

    var i = 0
    while (i < expenses.size) {
        val groupDate = expenses[i].getLocalDate()

        // Insert "Today" if we reached it or passed it
        if (!todayAdded && !groupDate.isAfter(today)) {
            if (groupDate.isBefore(today)) {
                // Today is missing, add empty today block
                addTodayBlock(result, today)
            }
            todayAdded = true
        }

        // Find group end and calculate total
        var j = i
        var groupTotal = 0.0
        while (j < expenses.size && expenses[j].getLocalDate() == groupDate) {
            groupTotal += expenses[j].price
            j++
        }

        // Add Total for the group
        val totId = "total_${groupDate.dayOfMonth}_${groupDate.monthValue}_${groupDate.year}"
        result.add(Expense(
            name = FirestoreEnums.NAMES.TOTAL.value,
            price = groupTotal,
            year = groupDate.year,
            month = groupDate.monthValue,
            day = groupDate.dayOfMonth,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            id = totId
        ))

        // Add expenses in group
        for (k in i until j) {
            result.add(expenses[k])
        }

        i = j
    }

    // If today was never reached (all future expenses)
    if (!todayAdded) {
        addTodayBlock(result, today)
    }

    return result
}

private fun addTodayBlock(result: MutableList<Expense>, todayDate: LocalDate) {
    val totId = "${todayDate.dayOfMonth}_${todayDate.monthValue}_${todayDate.year}"
    result.add(Expense(
        name = FirestoreEnums.NAMES.TOTAL.value,
        price = 0.0,
        year = todayDate.year,
        month = todayDate.monthValue,
        day = todayDate.dayOfMonth,
        category = FirestoreEnums.CATEGORIES.TOTAL.value,
        id = "total_$totId"
    ))
    result.add(Expense(
        name = "",
        price = 0.0,
        year = todayDate.year,
        month = todayDate.monthValue,
        day = todayDate.dayOfMonth,
        category = FirestoreEnums.CATEGORIES.JOLLY.value,
        id = "jolly_$totId"
    ))
}

fun addTotalsToExpensesWithoutToday(expenses: List<Expense>): List<Expense> {
    val expenseList = mutableListOf<Expense>()
    if (expenses.isEmpty()) return expenseList
    // Used to keep the order
    var currentExpenses = mutableListOf<Expense>()

    // Create total for the local list
    var total = Expense(
        name = FirestoreEnums.NAMES.TOTAL.value,
        price = 0.0,
        year = expenses[0].year,
        month = expenses[0].month,
        day = expenses[0].day,
        category = FirestoreEnums.CATEGORIES.TOTAL.value,
        id = "total_${expenses[0].getTotalId()}"
    )

    expenses.forEach { expense ->
        if (total.id == "total_${expense.getTotalId()}") { // Update the total
            total = Expense(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = total.price + expense.price,
                year = expense.year,
                month = expense.month,
                day = expense.day,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = total.id
            )
            currentExpenses.add(expense)
        } else { // We need a new total
            // Update the local list with previous day expenses
            if (currentExpenses.isNotEmpty()) {
                expenseList.add(total)
                currentExpenses.forEach { p ->
                    expenseList.add(p)
                }
            }
            currentExpenses = mutableListOf()
            currentExpenses.add(expense)
            // Create new total
            total = Expense(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = expense.price,
                year = expense.year,
                month = expense.month,
                day = expense.day,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = "total_${expense.getTotalId()}"
            )
        }
    }
    if (currentExpenses.isNotEmpty()) {
        expenseList.add(total)
        currentExpenses.forEach { p ->
            expenseList.add(p)
        }
    }
    return expenseList
}

fun addTotalsToIncomes(incomes: List<Income>, today: LocalDate = LocalDate.now()): List<Income> {
    val incomeList = mutableListOf<Income>()

    var total = Income(
        name = FirestoreEnums.NAMES.TOTAL.value,
        price = 0.0,
        year = today.year,
        month = 0,
        day = 0,
        category = FirestoreEnums.CATEGORIES.TOTAL.value,
        id = "total_${today.year}"
    )
    var currentIncomes = mutableListOf<Income>()

    var isFirstIncome = true
    incomes.forEach { income ->
        if (isFirstIncome && today.year > income.year) {
            // Inserisci totale a 0.0 per oggi
            incomeList.add(total)
            // Add empty income (with random name, jolly category, and price to 0.0)
            total = Income(
                name = "",
                price = 0.0,
                year = today.year,
                month = today.monthValue,
                day = today.dayOfMonth,
                category = FirestoreEnums.CATEGORIES.JOLLY.value,
                id = "jolly_${today.year}"
            )
            incomeList.add(total)
            total = Income(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = 0.0,
                year = income.year,
                month = 0,
                day = 0,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = "total_${income.year}"
            )
        } else if (isFirstIncome && income.year > today.year) {
            total = Income(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = 0.0,
                year = income.year,
                month = 0,
                day = 0,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = "total_${income.year}"
            )
        }

        isFirstIncome = false

        // Popola lista
        if (total.year == income.year) { // Se totale corrisponde, aggiorna
            total = Income(
                name = total.name,
                price = total.price + income.price,
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
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = income.price,
                year = income.year,
                month = 0,
                day = 0,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = "total_${income.year}"
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

/**
 * One entry per month for the [count] months ending with [lastMonth], oldest first.
 * Months missing from [entries] get a zero entry, so a chart shows gaps as empty bars.
 */
fun fillMonthlyGaps(entries: List<MonthlyTotal>, lastMonth: LocalDate, count: Int): List<MonthlyTotal> {
    val byMonth = entries.associateBy { it.year * 100 + it.month }
    return (count - 1 downTo 0).map { offset ->
        val date = lastMonth.minusMonths(offset.toLong())
        byMonth[date.year * 100 + date.monthValue] ?: MonthlyTotal(0.0, date.year, date.monthValue)
    }
}

/** How expense names are matched: case and surrounding spaces don't matter. */
fun expenseNameKey(name: String): String = name.trim().lowercase()
