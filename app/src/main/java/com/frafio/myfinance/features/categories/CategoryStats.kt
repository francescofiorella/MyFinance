package com.frafio.myfinance.features.categories

import com.frafio.myfinance.core.data.model.CategorySum
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.data.model.SpendingChange
import com.frafio.myfinance.core.data.model.categoriesTotal
import com.frafio.myfinance.core.data.model.spendingChange

data class CategorySpending(
    val category: ExpenseCategory,
    val total: Double,
    /** Fraction of the month's total, from 0 to 1. */
    val share: Double,
    val change: SpendingChange
)

/**
 * One entry per category, highest total first; categories with nothing spent are included with 0.
 * Ties keep the order of [ExpenseCategories.all]. Sums for ids that aren't a category are ignored.
 */
fun buildCategorySpending(current: List<CategorySum>, previous: List<CategorySum>): List<CategorySpending> {
    val currentTotals = current.associate { it.category to it.total }
    val previousTotals = previous.associate { it.category to it.total }
    val monthTotal = current.categoriesTotal()
    return ExpenseCategories.all
        .map { category ->
            val total = currentTotals[category.id] ?: 0.0
            CategorySpending(
                category = category,
                total = total,
                share = if (monthTotal > 0.0) total / monthTotal else 0.0,
                change = spendingChange(total, previousTotals[category.id] ?: 0.0)
            )
        }
        .sortedByDescending { it.total }
}
