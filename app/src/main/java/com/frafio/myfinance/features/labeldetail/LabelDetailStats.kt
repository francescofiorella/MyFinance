package com.frafio.myfinance.features.labeldetail

import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.features.categorydetail.FrequentExpense
import com.frafio.myfinance.features.categorydetail.TOP_EXPENSES_COUNT
import com.frafio.myfinance.features.categorydetail.frequentExpenses
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class CategoryShare(
    val category: ExpenseCategory,
    val total: Double,
    /** Fraction of the label's total, from 0 to 1. */
    val share: Double
)

data class DayTotal(
    val date: LocalDate,
    val total: Double
)

/** Statistics over a label's whole lifetime. */
data class LabelDetailStats(
    val total: Double,
    val count: Int,
    val averagePerExpense: Double,
    val first: LocalDate,
    val last: LocalDate,
    /** Days from [first] to [last], both included. */
    val spanDays: Int,
    /** Days with at least one expense. */
    val activeDays: Int,
    val averagePerActiveDay: Double,
    /** The day with the highest total; the most recent one on a tie. */
    val biggestDay: DayTotal,
    /** The categories with spending, highest first. */
    val categories: List<CategoryShare>,
    val topExpenses: List<Expense>,
    val frequent: List<FrequentExpense>
)

/** Statistics of a label's [expenses], newest first; null without expenses. */
fun buildLabelDetail(expenses: List<Expense>): LabelDetailStats? {
    if (expenses.isEmpty()) return null
    val total = expenses.sumOf { it.price }
    val days = expenses
        .groupBy { it.getLocalDate() }
        .map { (date, dayExpenses) -> DayTotal(date, dayExpenses.sumOf { it.price }) }
    val first = days.minOf { it.date }
    val last = days.maxOf { it.date }
    return LabelDetailStats(
        total = total,
        count = expenses.size,
        averagePerExpense = total / expenses.size,
        first = first,
        last = last,
        spanDays = ChronoUnit.DAYS.between(first, last).toInt() + 1,
        activeDays = days.size,
        averagePerActiveDay = total / days.size,
        biggestDay = days.maxWith(compareBy<DayTotal> { it.total }.thenBy { it.date }),
        categories = categoryShares(expenses, total),
        topExpenses = expenses.sortedByDescending { it.price }.take(TOP_EXPENSES_COUNT),
        frequent = frequentExpenses(expenses)
    )
}

/** The categories of [expenses] with spending, highest first; ties keep the order of [ExpenseCategories.all]. */
internal fun categoryShares(expenses: List<Expense>, total: Double): List<CategoryShare> {
    val totals = expenses.groupBy { it.category }.mapValues { (_, categoryExpenses) -> categoryExpenses.sumOf { it.price } }
    return ExpenseCategories.all
        .mapNotNull { category ->
            totals[category.id]?.takeIf { it > 0.0 }?.let { categoryTotal ->
                CategoryShare(category, categoryTotal, if (total > 0.0) categoryTotal / total else 0.0)
            }
        }
        .sortedByDescending { it.total }
}
