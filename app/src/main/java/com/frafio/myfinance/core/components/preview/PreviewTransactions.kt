package com.frafio.myfinance.core.components.preview

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income

/** Sample data for @Preview functions. Tests use testing/data/TestTransactions instead. */
object PreviewTransactions {

    val placeholderExpense = Expense(name = "Expense", price = 0.0, year = 1970, month = 1, day = 1)

    val pizza = Expense(
        name = "Pizza",
        price = 13.0,
        category = FirestoreEnums.CATEGORIES.DINING.value,
        day = 1,
        month = 1,
        year = 2024,
        labels = listOf("Dinner", "Cheat Meal")
    )

    val cola = Expense(
        name = "Cola",
        price = 2.0,
        category = FirestoreEnums.CATEGORIES.DINING.value,
        day = 1,
        month = 1,
        year = 2024
    )

    val salary = Income(
        name = "Salary",
        price = 2500.0,
        category = FirestoreEnums.CATEGORIES.INCOME.value,
        day = 1,
        month = 1,
        year = 2024
    )

    fun expenseTotal(price: Double, day: Int) = Expense(
        day = day,
        month = 1,
        year = 2024,
        category = FirestoreEnums.CATEGORIES.TOTAL.value,
        price = price
    )

    val incomeTotal = Income(
        year = 2024,
        month = 1,
        day = 1,
        category = FirestoreEnums.CATEGORIES.TOTAL.value,
        price = 2500.0
    )

    /** Three days of the expenses list: totals, today's placeholder row and the expenses under them. */
    val expensesByDay = listOf(
        Expense(
            name = FirestoreEnums.NAMES.TOTAL.value,
            price = 0.0,
            year = 2023,
            month = 10,
            day = 28,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            id = "total_28_10_2023",
        ),
        Expense(
            name = "Jolly",
            price = 0.0,
            year = 2023,
            month = 10,
            day = 28,
            category = FirestoreEnums.CATEGORIES.JOLLY.value,
            id = "0"
        ),
        Expense(
            name = FirestoreEnums.NAMES.TOTAL.value,
            price = 45.0,
            year = 2023,
            month = 10,
            day = 27,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            id = "total_27_10_2023"
        ),
        Expense(
            name = "Pizza Margherita",
            price = 8.5,
            year = 2023,
            month = 10,
            day = 27,
            category = FirestoreEnums.CATEGORIES.DINING.value,
            id = "1"
        ),
        Expense(
            name = "Groceries",
            price = 36.5,
            year = 2023,
            month = 10,
            day = 27,
            category = FirestoreEnums.CATEGORIES.GROCERIES.value,
            id = "2"
        ),
        Expense(
            name = FirestoreEnums.NAMES.TOTAL.value,
            price = 15.0,
            year = 2023,
            month = 10,
            day = 26,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            id = "total_26_10_2023"
        ),
        Expense(
            name = "Bus Ticket",
            price = 1.5,
            year = 2023,
            month = 10,
            day = 26,
            category = FirestoreEnums.CATEGORIES.TRANSPORTATION.value,
            id = "3"
        )
    )

    /** One year of the incomes list: its total and two incomes. */
    val incomesOfYear = listOf(
        Income(
            id = "2024",
            year = 2024,
            month = 0,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            price = 12000.0,
        ),
        Income(
            id = "1",
            name = "Salary",
            price = 1000.0,
            day = 1,
            month = 1,
            year = 2024,
            category = 0,
        ),
        Income(
            id = "2",
            name = "Freelance",
            price = 500.0,
            day = 15,
            month = 1,
            year = 2024,
            category = 0,
        )
    )
}
