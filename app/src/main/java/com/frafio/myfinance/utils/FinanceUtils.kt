package com.frafio.myfinance.utils

import androidx.compose.ui.graphics.Color
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.theme.brownContainerDark
import com.frafio.myfinance.ui.theme.brownContainerLight
import com.frafio.myfinance.ui.theme.brownOnContainerDark
import com.frafio.myfinance.ui.theme.brownOnContainerLight
import com.frafio.myfinance.ui.theme.indigoContainerDark
import com.frafio.myfinance.ui.theme.indigoContainerLight
import com.frafio.myfinance.ui.theme.indigoOnContainerDark
import com.frafio.myfinance.ui.theme.indigoOnContainerLight
import com.frafio.myfinance.ui.theme.lightBlueContainerDark
import com.frafio.myfinance.ui.theme.lightBlueContainerLight
import com.frafio.myfinance.ui.theme.lightBlueOnContainerDark
import com.frafio.myfinance.ui.theme.lightBlueOnContainerLight
import com.frafio.myfinance.ui.theme.lightGreenContainerDark
import com.frafio.myfinance.ui.theme.lightGreenContainerLight
import com.frafio.myfinance.ui.theme.lightGreenOnContainerDark
import com.frafio.myfinance.ui.theme.lightGreenOnContainerLight
import com.frafio.myfinance.ui.theme.orangeContainerDark
import com.frafio.myfinance.ui.theme.orangeContainerLight
import com.frafio.myfinance.ui.theme.orangeOnContainerDark
import com.frafio.myfinance.ui.theme.orangeOnContainerLight
import com.frafio.myfinance.ui.theme.purpleContainerDark
import com.frafio.myfinance.ui.theme.purpleContainerLight
import com.frafio.myfinance.ui.theme.purpleOnContainerDark
import com.frafio.myfinance.ui.theme.purpleOnContainerLight
import com.frafio.myfinance.ui.theme.redContainerDark
import com.frafio.myfinance.ui.theme.redContainerLight
import com.frafio.myfinance.ui.theme.redOnContainerDark
import com.frafio.myfinance.ui.theme.redOnContainerLight
import com.frafio.myfinance.ui.theme.tealContainerDark
import com.frafio.myfinance.ui.theme.tealContainerLight
import com.frafio.myfinance.ui.theme.tealOnContainerDark
import com.frafio.myfinance.ui.theme.tealOnContainerLight
import com.frafio.myfinance.ui.theme.yellowContainerDark
import com.frafio.myfinance.ui.theme.yellowContainerLight
import com.frafio.myfinance.ui.theme.yellowOnContainerDark
import com.frafio.myfinance.ui.theme.yellowOnContainerLight
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

fun getCategoryContainerColor(categoryId: Int?, default: Color, isDark: Boolean): Color {
    return when (categoryId) {
        FirestoreEnums.CATEGORIES.HOUSING.value -> if (!isDark) redContainerLight else redContainerDark
        FirestoreEnums.CATEGORIES.GROCERIES.value -> if (!isDark) purpleContainerLight else purpleContainerDark
        FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> if (!isDark) indigoContainerLight else indigoContainerDark
        FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> if (!isDark) lightBlueContainerLight else lightBlueContainerDark
        FirestoreEnums.CATEGORIES.EDUCATION.value -> if (!isDark) tealContainerLight else tealContainerDark
        FirestoreEnums.CATEGORIES.DINING.value -> if (!isDark) lightGreenContainerLight else lightGreenContainerDark
        FirestoreEnums.CATEGORIES.HEALTH.value -> if (!isDark) yellowContainerLight else yellowContainerDark
        FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> if (!isDark) orangeContainerLight else orangeContainerDark
        FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> if (!isDark) brownContainerLight else brownContainerDark
        else -> default
    }
}

fun getCategoryOnContainerColor(categoryId: Int?, default: Color, isDark: Boolean): Color {
    return when (categoryId) {
        FirestoreEnums.CATEGORIES.HOUSING.value -> if (!isDark) redOnContainerLight else redOnContainerDark
        FirestoreEnums.CATEGORIES.GROCERIES.value -> if (!isDark) purpleOnContainerLight else purpleOnContainerDark
        FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> if (!isDark) indigoOnContainerLight else indigoOnContainerDark
        FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> if (!isDark) lightBlueOnContainerLight else lightBlueOnContainerDark
        FirestoreEnums.CATEGORIES.EDUCATION.value -> if (!isDark) tealOnContainerLight else tealOnContainerDark
        FirestoreEnums.CATEGORIES.DINING.value -> if (!isDark) lightGreenOnContainerLight else lightGreenOnContainerDark
        FirestoreEnums.CATEGORIES.HEALTH.value -> if (!isDark) yellowOnContainerLight else yellowOnContainerDark
        FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> if (!isDark) orangeOnContainerLight else orangeOnContainerDark
        FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> if (!isDark) brownOnContainerLight else brownOnContainerDark
        else -> default
    }
}

fun getCategoryTextColor(categoryId: Int?, default: Color, isDark: Boolean): Color {
    return when (categoryId) {
        FirestoreEnums.CATEGORIES.HOUSING.value -> if (!isDark) redContainerLight else redOnContainerDark
        FirestoreEnums.CATEGORIES.GROCERIES.value -> if (!isDark) purpleContainerLight else purpleOnContainerDark
        FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> if (!isDark) indigoContainerLight else indigoOnContainerDark
        FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> if (!isDark) lightBlueContainerLight else lightBlueOnContainerDark
        FirestoreEnums.CATEGORIES.EDUCATION.value -> if (!isDark) tealContainerLight else tealOnContainerDark
        FirestoreEnums.CATEGORIES.DINING.value -> if (!isDark) lightGreenContainerLight else lightGreenOnContainerDark
        FirestoreEnums.CATEGORIES.HEALTH.value -> if (!isDark) yellowContainerLight else yellowOnContainerDark
        FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> if (!isDark) orangeContainerLight else orangeOnContainerDark
        FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> if (!isDark) brownContainerLight else brownOnContainerDark
        else -> default
    }
}

fun addTotalsToExpenses(expenses: List<Expense>): List<Expense> {
    if (expenses.isEmpty()) return emptyList()

    val result = ArrayList<Expense>(expenses.size + 10)
    val todayDate = LocalDate.now()
    var todayAdded = false

    var i = 0
    while (i < expenses.size) {
        val groupDate = expenses[i].getLocalDate()

        // Insert "Today" if we reached it or passed it
        if (!todayAdded && !groupDate.isAfter(todayDate)) {
            if (groupDate.isBefore(todayDate)) {
                // Today is missing, add empty today block
                addTodayBlock(result, todayDate)
            }
            todayAdded = true
        }

        // Find group end and calculate total
        var j = i
        var groupTotal = 0.0
        while (j < expenses.size && expenses[j].getLocalDate() == groupDate) {
            groupTotal += (expenses[j].price ?: 0.0)
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
        addTodayBlock(result, todayDate)
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
                price = total.price!! + expense.price!!,
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