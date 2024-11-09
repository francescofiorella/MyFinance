package com.frafio.myfinance.utils

import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.Expense
import java.time.LocalDate

fun addTotalsToExpenses(expenses: List<Expense>): List<Expense> {
    val expenseList = mutableListOf<Expense>()
    // Create total for the local list
    var total: Expense? = null
    // Used to keep the order
    var currentExpenses = mutableListOf<Expense>()

    var prevDate: LocalDate? = null

    expenses.forEach { expense ->
        val todayDate = LocalDate.now()
        val expenseDate = expense.getLocalDate()
        total?.let {
            prevDate = total!!.getLocalDate()
        }

        if ((prevDate == null || prevDate!!.isAfter(todayDate)) &&
            expenseDate.isBefore(todayDate)
        ) {
            if (currentExpenses.isNotEmpty()) {
                expenseList.add(total!!)
                currentExpenses.forEach { p ->
                    expenseList.add(p)
                }
            }
            currentExpenses = mutableListOf()
            // Aggiungi totale a 0 per oggi
            val totId = "${todayDate.dayOfMonth}_${todayDate.monthValue}_${todayDate.year}"
            total = Expense(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = 0.0,
                year = todayDate.year,
                month = todayDate.monthValue,
                day = todayDate.dayOfMonth,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = totId
            )
            expenseList.add(total!!)
            // Add empty expense (with random name, jolly category, and price to 0.0)
            total = Expense(
                name = "",
                price = 0.0,
                year = todayDate.year,
                month = todayDate.monthValue,
                day = todayDate.dayOfMonth,
                category = FirestoreEnums.CATEGORIES.JOLLY.value,
                id = totId
            )
            expenseList.add(total!!)
            prevDate = total!!.getLocalDate()
        }

        if (prevDate == null) { // If is the first total
            currentExpenses.add(expense)
            total = Expense(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = expense.price,
                year = expense.year,
                month = expense.month,
                day = expense.day,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = expense.getTotalId()
            )
        } else if (total!!.id == expense.getTotalId()) { // If the total should be updated
            currentExpenses.add(expense)
            total = Expense(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = total!!.price!! + expense.price!!,
                year = total!!.year,
                month = total!!.month,
                day = total!!.day,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = total!!.getTotalId()
            )
        } else { // If we need a new total
            // Update the local list with previous day expenses
            if (currentExpenses.isNotEmpty()) {
                expenseList.add(total!!)
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
                id = expense.getTotalId()
            )
        }
    }
    if (currentExpenses.isNotEmpty()) {
        expenseList.add(total!!)
        currentExpenses.forEach { p ->
            expenseList.add(p)
        }
    }
    return expenseList
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
        id = expenses[0].getTotalId()
    )

    expenses.forEach { expense ->
        if (total.id == expense.getTotalId()) { // Update the total
            total = Expense(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = total.price!! + expense.price!!,
                year = expense.year,
                month = expense.month,
                day = expense.day,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = expense.getTotalId()
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
                id = expense.getTotalId()
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

fun addTotalsToIncomes(incomes: List<Income>): List<Income> {
    val incomeList = mutableListOf<Income>()

    val todayYear = LocalDate.now().year
    var total = Income(
        name = FirestoreEnums.NAMES.TOTAL.value,
        price = 0.0,
        year = todayYear,
        month = 0,
        day = 0,
        category = FirestoreEnums.CATEGORIES.TOTAL.value,
        id = todayYear.toString()
    )
    var currentIncomes = mutableListOf<Income>()

    var isFirstIncome = true
    incomes.forEach { income ->
        if (isFirstIncome && todayYear > income.year!!) {
            // Inserisci totale a 0.0 per oggi
            incomeList.add(total)
            // Add empty income (with random name, jolly category, and price to 0.0)
            total = Income(
                name = "",
                price = 0.0,
                year = todayYear,
                month = 0,
                day = 0,
                category = FirestoreEnums.CATEGORIES.JOLLY.value,
                id = todayYear.toString()
            )
            incomeList.add(total)
            total = Income(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = 0.0,
                year = income.year,
                month = 0,
                day = 0,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = income.year.toString()
            )
        } else if (isFirstIncome && income.year!! > todayYear) {
            total = Income(
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = 0.0,
                year = income.year,
                month = 0,
                day = 0,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
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
                name = FirestoreEnums.NAMES.TOTAL.value,
                price = income.price,
                year = income.year,
                month = 0,
                day = 0,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
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