package com.frafio.myfinance.testing.data

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.repository.UserPreferencesData
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import java.time.LocalDate

// year/month/day and timestamp are always derived from the same date so a fixture
// can never disagree with itself the way a hand-built Expense can.
fun testExpense(
    name: String = "Coffee",
    price: Double = 1.5,
    date: LocalDate = LocalDate.of(2024, 1, 15),
    category: Int = FirestoreEnums.CATEGORIES.DINING.value,
    labels: List<String> = emptyList(),
    id: String = "expense-$name-$date",
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

fun testIncome(
    name: String = "Salary",
    price: Double = 1000.0,
    date: LocalDate = LocalDate.of(2024, 1, 31),
    category: Int = FirestoreEnums.CATEGORIES.INCOME.value,
    id: String = "income-$name-$date",
): Income = Income(
    name = name,
    price = price,
    year = date.year,
    month = date.monthValue,
    day = date.dayOfMonth,
    timestamp = dateToUTCTimestamp(date),
    category = category,
    id = id,
)

fun testUser(
    email: String = "ada@example.com",
    fullName: String? = "Ada Lovelace",
    photoUrl: String? = null,
    provider: Int = User.EMAIL_PROVIDER,
    providers: List<String> = listOf("password"),
    hasPassword: Boolean = true,
    isGoogleLinked: Boolean = false,
): User = User(
    fullName = fullName,
    email = email,
    photoUrl = photoUrl,
    provider = provider,
    providers = providers,
    hasPassword = hasPassword,
    isGoogleLinked = isGoogleLinked,
    creationYear = 2024,
    creationMonth = 1,
    creationDay = 1,
)

fun testPreferences(
    dynamicColor: Boolean = true,
    monthlyBudget: Double = 0.0,
    currencyCode: String = "EUR",
    labels: List<String> = emptyList(),
    user: User? = null,
    proPicChoice: String? = null,
): UserPreferencesData = UserPreferencesData(
    dynamicColor = dynamicColor,
    monthlyBudget = monthlyBudget,
    currencyCode = currencyCode,
    labels = labels,
    user = user,
    proPicChoice = proPicChoice,
)
