package com.frafio.myfinance.utils

import androidx.compose.ui.graphics.Color
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.theme.brown500
import com.frafio.myfinance.ui.theme.indigo500
import com.frafio.myfinance.ui.theme.lightBlue500
import com.frafio.myfinance.ui.theme.lightGreen500
import com.frafio.myfinance.ui.theme.orange500
import com.frafio.myfinance.ui.theme.purple500
import com.frafio.myfinance.ui.theme.red500
import com.frafio.myfinance.ui.theme.teal500
import com.frafio.myfinance.ui.theme.yellow500
import java.time.LocalDate

fun getCategoryIcon(categoryId: Int?): Int {
    return when (categoryId) {
        FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_home_filled
        FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart_filled
        FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care_filled
        FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy_filled
        FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school_filled
        FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant_filled
        FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines_filled
        FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_subway_filled
        FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_grid_3x3_filled
        else -> R.drawable.ic_grid_3x3_filled
    }
}

fun getCategoryName(categoryId: Int?): Int {
    return when (categoryId) {
        FirestoreEnums.CATEGORIES.HOUSING.value -> R.string.housing
        FirestoreEnums.CATEGORIES.GROCERIES.value -> R.string.groceries
        FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.string.personal_care
        FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.string.entertainment
        FirestoreEnums.CATEGORIES.EDUCATION.value -> R.string.education
        FirestoreEnums.CATEGORIES.DINING.value -> R.string.dining
        FirestoreEnums.CATEGORIES.HEALTH.value -> R.string.health
        FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.string.transportation
        FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.string.miscellaneous
        else -> R.string.category
    }
}

fun getCategoryColor(categoryId: Int?, default: Color): Color {
    return when (categoryId) {
        FirestoreEnums.CATEGORIES.HOUSING.value -> red500
        FirestoreEnums.CATEGORIES.GROCERIES.value -> purple500
        FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> indigo500
        FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> lightBlue500
        FirestoreEnums.CATEGORIES.EDUCATION.value -> teal500
        FirestoreEnums.CATEGORIES.DINING.value -> lightGreen500
        FirestoreEnums.CATEGORIES.HEALTH.value -> yellow500
        FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> orange500
        FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> brown500
        else -> default
    }
}

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

        if ((prevDate == null || prevDate.isAfter(todayDate)) &&
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
            expenseList.add(total)
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
            expenseList.add(total)
            prevDate = total.getLocalDate()
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
                price = total.price!! + expense.price!!,
                year = total.year,
                month = total.month,
                day = total.day,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                id = total.getTotalId()
            )
        } else { // If we need a new total
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

    val todayDate = LocalDate.now()
    var total = Income(
        name = FirestoreEnums.NAMES.TOTAL.value,
        price = 0.0,
        year = todayDate.year,
        month = 0,
        day = 0,
        category = FirestoreEnums.CATEGORIES.TOTAL.value,
        id = todayDate.year.toString()
    )
    var currentIncomes = mutableListOf<Income>()

    var isFirstIncome = true
    incomes.forEach { income ->
        if (isFirstIncome && todayDate.year > income.year!!) {
            // Inserisci totale a 0.0 per oggi
            incomeList.add(total)
            // Add empty income (with random name, jolly category, and price to 0.0)
            total = Income(
                name = "",
                price = 0.0,
                year = todayDate.year,
                month = todayDate.monthValue,
                day = todayDate.dayOfMonth,
                category = FirestoreEnums.CATEGORIES.JOLLY.value,
                id = todayDate.year.toString()
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
        } else if (isFirstIncome && income.year!! > todayDate.year) {
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