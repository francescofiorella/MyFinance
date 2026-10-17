package com.frafio.myfinance.features.categories

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.CategorySum
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.SpendingChange
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CategoryStatsTest {

    private val housing = CATEGORIES.HOUSING.value
    private val groceries = CATEGORIES.GROCERIES.value
    private val dining = CATEGORIES.DINING.value

    // region buildCategorySpending

    @Test
    fun buildCategorySpending_listsEveryCategory_highestFirst_tiesInListOrder() {
        val spending = buildCategorySpending(
            current = listOf(CategorySum(dining, 30.0), CategorySum(housing, 70.0)),
            previous = emptyList()
        )

        assertThat(spending.map { it.category.id }).containsExactly(
            housing, dining,
            // Nothing spent: the order of ExpenseCategories.all.
            *ExpenseCategories.ids.filter { it != housing && it != dining }.toTypedArray()
        ).inOrder()
        assertThat(spending.first().share).isEqualTo(0.7)
        assertThat(spending.last().total).isEqualTo(0.0)
        assertThat(spending.last().share).isEqualTo(0.0)
    }

    @Test
    fun buildCategorySpending_ignoresIdsThatAreNotCategories() {
        val spending = buildCategorySpending(
            current = listOf(CategorySum(housing, 50.0), CategorySum(CATEGORIES.TOTAL.value, 1000.0), CategorySum(-1, 50.0)),
            previous = emptyList()
        )

        assertThat(spending).hasSize(ExpenseCategories.all.size)
        assertThat(spending.sumOf { it.total }).isEqualTo(50.0)
        assertThat(spending.first().share).isEqualTo(1.0)
    }

    @Test
    fun buildCategorySpending_comparesWithThePreviousMonth() {
        val spending = buildCategorySpending(
            current = listOf(CategorySum(housing, 120.0), CategorySum(dining, 40.0)),
            previous = listOf(CategorySum(housing, 100.0), CategorySum(groceries, 80.0))
        ).associateBy { it.category.id }

        assertThat(spending.getValue(housing).change).isEqualTo(SpendingChange.Percent(0.2))
        assertThat(spending.getValue(dining).change).isEqualTo(SpendingChange.New)
        assertThat(spending.getValue(groceries).change).isEqualTo(SpendingChange.Percent(-1.0))
        assertThat(spending.getValue(CATEGORIES.HEALTH.value).change).isEqualTo(SpendingChange.None)
    }

    @Test
    fun buildCategorySpending_withNoSpending_hasZeroShares() {
        val spending = buildCategorySpending(emptyList(), emptyList())

        assertThat(spending.map { it.share }.distinct()).containsExactly(0.0)
    }

    // endregion
}
