package com.frafio.myfinance.testing.data

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import java.time.LocalDate

// androidTest cannot see src/test, so these mirror TestTransactions.kt: y/m/d and timestamp always
// come from the same date.
fun expense(
    name: String = "Coffee",
    price: Double = 1.5,
    date: LocalDate = LocalDate.of(2024, 1, 15),
    category: Int = FirestoreEnums.CATEGORIES.DINING.value,
    labels: List<String> = emptyList(),
    id: String = "$name-$date",
): Expense = Expense(
    name = name,
    price = price,
    year = date.year,
    month = date.monthValue,
    day = date.dayOfMonth,
    timestamp = dateToUTCTimestamp(date),
    category = category,
    labels = labels,
    id = id,
)

fun income(
    name: String = "Salary",
    price: Double = 1000.0,
    date: LocalDate = LocalDate.of(2024, 1, 31),
    id: String = "$name-$date",
): Income = Income(
    name = name,
    price = price,
    year = date.year,
    month = date.monthValue,
    day = date.dayOfMonth,
    timestamp = dateToUTCTimestamp(date),
    category = FirestoreEnums.CATEGORIES.INCOME.value,
    id = id,
)
