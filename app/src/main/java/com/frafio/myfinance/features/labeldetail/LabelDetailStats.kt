package com.frafio.myfinance.features.labeldetail

import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.features.categorydetail.FrequentExpense
import com.frafio.myfinance.features.categorydetail.TOP_EXPENSES_COUNT
import com.frafio.myfinance.features.categorydetail.frequentExpenses
import java.time.DayOfWeek
import java.time.LocalDate

private val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

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
    /** Days with at least one expense. */
    val activeDays: Int,
    val averagePerActiveDay: Double,
    /** Spent on Saturdays and Sundays. */
    val weekendTotal: Double,
    /** [weekendTotal] as a fraction of [total], from 0 to 1. */
    val weekendShare: Double,
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
    val weekendTotal = days.filter { it.date.dayOfWeek in WEEKEND }.sumOf { it.total }
    return LabelDetailStats(
        total = total,
        count = expenses.size,
        averagePerExpense = total / expenses.size,
        first = days.minOf { it.date },
        last = days.maxOf { it.date },
        activeDays = days.size,
        averagePerActiveDay = total / days.size,
        weekendTotal = weekendTotal,
        weekendShare = if (total > 0.0) weekendTotal / total else 0.0,
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
