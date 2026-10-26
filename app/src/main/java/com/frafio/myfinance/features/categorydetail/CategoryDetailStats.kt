package com.frafio.myfinance.features.categorydetail

import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.data.model.SpendingChange
import com.frafio.myfinance.core.data.model.spendingChange
import com.frafio.myfinance.core.utils.expenseNameKey
import com.frafio.myfinance.core.utils.fillMonthlyGaps
import java.time.LocalDate

/** Months covered by a category's stats (monthly average, frequent expenses), ending with the selected month. */
const val TREND_MONTHS = 12
const val TOP_EXPENSES_COUNT = 5
const val FREQUENT_EXPENSES_COUNT = 3

data class FrequentExpense(
    val name: String,
    val count: Int,
    val total: Double
)

data class LabelSpending(
    val label: String,
    val total: Double
)

data class CategoryDetailStats(
    val monthTotal: Double,
    val change: SpendingChange,
    /** The [TREND_MONTHS] window's total divided by [TREND_MONTHS]. */
    val average: Double,
    val yearToDate: Double,
    /** Fraction of everything spent in the month, from 0 to 1. */
    val share: Double,
    /** [TREND_MONTHS] entries, oldest first; the trend chart loads its own, longer run of months. */
    val trend: List<MonthlyTotal>,
    val topExpenses: List<Expense>,
    val frequent: List<FrequentExpense>,
    val labels: List<LabelSpending>
)

/**
 * Statistics for one category.
 *
 * @param month the selected month; any day of it.
 * @param windowExpenses the category's expenses in the [TREND_MONTHS] months ending with [month], newest first.
 * @param monthTotalAll everything spent in [month] across all categories.
 */
fun buildCategoryDetail(month: LocalDate, windowExpenses: List<Expense>, monthTotalAll: Double): CategoryDetailStats {
    val trend = monthlyTotals(windowExpenses, lastMonth = month, count = TREND_MONTHS)
    val monthTotal = trend.last().value
    val previousTotal = trend[trend.size - 2].value
    val monthExpenses = windowExpenses.filter { it.year == month.year && it.month == month.monthValue }

    return CategoryDetailStats(
        monthTotal = monthTotal,
        change = spendingChange(monthTotal, previousTotal),
        average = trend.sumOf { it.value } / TREND_MONTHS,
        yearToDate = trend.filter { it.year == month.year }.sumOf { it.value },
        share = if (monthTotalAll > 0.0) monthTotal / monthTotalAll else 0.0,
        trend = trend,
        topExpenses = monthExpenses.sortedByDescending { it.price }.take(TOP_EXPENSES_COUNT),
        frequent = frequentExpenses(windowExpenses),
        labels = labelSpending(monthExpenses)
    )
}

/** What [expenses] add up to in each of the [count] months ending with [lastMonth], oldest first; months without any are zero. */
internal fun monthlyTotals(expenses: List<Expense>, lastMonth: LocalDate, count: Int): List<MonthlyTotal> {
    val monthlySums = expenses
        .groupBy { it.year to it.month }
        .map { (yearMonth, monthExpenses) -> MonthlyTotal(monthExpenses.sumOf { it.price }, yearMonth.first, yearMonth.second) }
    return fillMonthlyGaps(monthlySums, lastMonth = lastMonth, count = count)
}

/**
 * Names used at least twice, matched ignoring case and surrounding spaces; most used first, then highest total.
 * Each keeps the spelling of its most recent expense, as [expenses] is newest first.
 */
internal fun frequentExpenses(expenses: List<Expense>): List<FrequentExpense> =
    expenses
        .filter { it.name.isNotBlank() }
        .groupBy { expenseNameKey(it.name) }
        .values
        .filter { it.size >= 2 }
        .map { group -> FrequentExpense(group.first().name.trim(), group.size, group.sumOf { it.price }) }
        .sortedWith(compareByDescending<FrequentExpense> { it.count }.thenByDescending { it.total })
        .take(FREQUENT_EXPENSES_COUNT)

/** Spending per label, highest first; an expense with several labels counts toward each of them. */
internal fun labelSpending(expenses: List<Expense>): List<LabelSpending> =
    expenses
        .flatMap { expense -> expense.labels.distinct().map { label -> label to expense.price } }
        .groupBy({ it.first }, { it.second })
        .map { (label, prices) -> LabelSpending(label, prices.sum()) }
        .sortedByDescending { it.total }
