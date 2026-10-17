package com.frafio.myfinance.core.data.model

import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExpenseCategoriesTest {

    @Test
    fun ids_matchPersistedCategoriesInOrder() {
        assertThat(ExpenseCategories.ids).containsExactly(
            CATEGORIES.HOUSING.value,
            CATEGORIES.GROCERIES.value,
            CATEGORIES.PERSONAL_CARE.value,
            CATEGORIES.ENTERTAINMENT.value,
            CATEGORIES.EDUCATION.value,
            CATEGORIES.DINING.value,
            CATEGORIES.HEALTH.value,
            CATEGORIES.TRANSPORTATION.value,
            CATEGORIES.MISCELLANEOUS.value
        ).inOrder()
    }

    @Test
    fun ids_areUnique() {
        assertThat(ExpenseCategories.ids).containsNoDuplicates()
    }

    @Test
    fun get_returnsMatchingCategory() {
        ExpenseCategories.all.forEach {
            assertThat(ExpenseCategories.get(it.id)).isEqualTo(it)
        }
    }

    @Test
    fun get_returnsUnknown_forNoneAndSentinelIds() {
        listOf(null, -1, CATEGORIES.TOTAL.value, CATEGORIES.INCOME.value, CATEGORIES.JOLLY.value).forEach {
            assertThat(ExpenseCategories.get(it)).isEqualTo(ExpenseCategories.Unknown)
        }
    }

    @Test
    fun unknown_keepsGenericFallback_andIsNotListed() {
        assertThat(ExpenseCategories.Unknown.iconRes).isEqualTo(R.drawable.ic_grid_3x3_filled)
        assertThat(ExpenseCategories.Unknown.nameRes).isEqualTo(R.string.category)
        assertThat(ExpenseCategories.all).doesNotContain(ExpenseCategories.Unknown)
    }
}
