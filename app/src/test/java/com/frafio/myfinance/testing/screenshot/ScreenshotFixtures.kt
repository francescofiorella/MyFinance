package com.frafio.myfinance.testing.screenshot

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import java.time.LocalDate

/** Fixed clock for every screenshot; nothing rendered may depend on the real date. */
val screenshotToday: LocalDate = LocalDate.of(2024, 5, 29)

private fun total(date: LocalDate, price: Double): Expense = Expense(
    name = FirestoreEnums.NAMES.TOTAL.value,
    price = price,
    year = date.year,
    month = date.monthValue,
    day = date.dayOfMonth,
    category = FirestoreEnums.CATEGORIES.TOTAL.value,
    id = "total_${date.dayOfMonth}_${date.monthValue}_${date.year}",
)

/** Two days of expenses with their total rows, as the Expenses list renders them. */
val screenshotExpenses: List<Expense> = listOf(
    total(screenshotToday, 45.0),
    testExpense(name = "Pizza Margherita", price = 8.5, date = screenshotToday, labels = listOf("Dinner", "Cheat Meal"), id = "1"),
    testExpense(name = "Groceries", price = 36.5, date = screenshotToday, category = FirestoreEnums.CATEGORIES.GROCERIES.value, id = "2"),
    total(screenshotToday.minusDays(1), 1.5),
    testExpense(name = "Bus Ticket", price = 1.5, date = screenshotToday.minusDays(1), category = FirestoreEnums.CATEGORIES.TRANSPORTATION.value, id = "3"),
)

val screenshotExpensesMetadata: Map<Int, Pair<Int, Int>> = mapOf(1 to (0 to 2), 2 to (1 to 2), 4 to (0 to 1))

val screenshotIncomes: List<Income> = listOf(
    Income(id = "2024", year = 2024, month = 0, category = FirestoreEnums.CATEGORIES.TOTAL.value, price = 12000.0),
    testIncome(name = "Salary", price = 1000.0, date = LocalDate.of(2024, 1, 1), id = "1"),
    testIncome(name = "Freelance", price = 500.0, date = LocalDate.of(2024, 1, 15), id = "2"),
)

val screenshotIncomesMetadata: Map<Int, Pair<Int, Int>> = mapOf(1 to (0 to 2), 2 to (1 to 2))

val screenshotBarChart: List<BarChartEntry> = listOf(
    120.0, 340.0, 210.0, 480.0, 150.0, 390.0, 260.0, 310.0, 430.0, 180.0, 290.0, 450.0,
).mapIndexed { index, value -> BarChartEntry(value = value, year = 2024, month = index + 1) }

val screenshotLabels: List<String> = listOf("Dinner", "Cheat Meal", "Work", "Holiday", "Gift")

fun screenshotDateLabel(start: LocalDate, end: LocalDate): String = "27 - 29 May 2024"
