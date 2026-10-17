package com.frafio.myfinance.core.data.model

/** Total spent in one category over a period, as returned by a GROUP BY category query. */
data class CategorySum(
    val category: Int,
    val total: Double
)

/** The total over the real categories only, so shares of it always add up to 100%. */
fun List<CategorySum>.categoriesTotal(): Double =
    filter { it.category in ExpenseCategories.ids }.sumOf { it.total }
