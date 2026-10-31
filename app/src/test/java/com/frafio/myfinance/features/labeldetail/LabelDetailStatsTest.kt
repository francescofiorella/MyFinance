package com.frafio.myfinance.features.labeldetail

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.features.categorydetail.FrequentExpense
import com.frafio.myfinance.testing.data.testExpense
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class LabelDetailStatsTest {

    private val aug12 = LocalDate.of(2025, 8, 12)
    private val aug14 = LocalDate.of(2025, 8, 14)

    @Test
    fun noExpenses_isNull() {
        assertThat(buildLabelDetail(emptyList())).isNull()
    }

    @Test
    fun totals_andAverages_perExpenseAndPerActiveDay() {
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Dinner", price = 30.0, date = aug14),
                testExpense(name = "Lunch", price = 20.0, date = aug12),
                testExpense(name = "Coffee", price = 10.0, date = aug12)
            )
        )!!

        assertThat(stats.total).isEqualTo(60.0)
        assertThat(stats.count).isEqualTo(3)
        assertThat(stats.averagePerExpense).isEqualTo(20.0)
        assertThat(stats.activeDays).isEqualTo(2)
        assertThat(stats.averagePerActiveDay).isEqualTo(30.0)
    }

    @Test
    fun dateRange_isFromTheFirstToTheLastExpense() {
        // A trip booked months ahead: two active days.
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Train", date = LocalDate.of(2025, 8, 19)),
                testExpense(name = "Flight", date = LocalDate.of(2025, 2, 3))
            )
        )!!

        assertThat(stats.first).isEqualTo(LocalDate.of(2025, 2, 3))
        assertThat(stats.last).isEqualTo(LocalDate.of(2025, 8, 19))
        assertThat(stats.activeDays).isEqualTo(2)
    }

    @Test
    fun singleExpense_startsAndEndsOnItsDay() {
        val stats = buildLabelDetail(listOf(testExpense(price = 5.0, date = aug12)))!!

        assertThat(stats.first).isEqualTo(aug12)
        assertThat(stats.last).isEqualTo(aug12)
        assertThat(stats.activeDays).isEqualTo(1)
        assertThat(stats.averagePerActiveDay).isEqualTo(5.0)
    }

    @Test
    fun topExpenses_areTheFiveHighest_tiesNewestFirst() {
        val expenses = (1..7).map { day ->
            testExpense(name = "E$day", price = if (day <= 2) 100.0 else day.toDouble(), date = LocalDate.of(2025, 8, day))
        }.reversed()

        val stats = buildLabelDetail(expenses)!!

        assertThat(stats.topExpenses.map { it.name }).containsExactly("E2", "E1", "E7", "E6", "E5").inOrder()
    }

    @Test
    fun frequent_areTheNamesUsedAtLeastTwice() {
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Ramen", price = 14.0, date = aug14),
                testExpense(name = "ramen ", price = 12.0, date = aug12),
                testExpense(name = "Museum", price = 20.0, date = aug12)
            )
        )!!

        assertThat(stats.frequent).containsExactly(FrequentExpense("Ramen", 2, 26.0))
    }

    @Test
    fun categories_withSpending_highestFirst_sharesSumToOne() {
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Dinner", price = 30.0, category = CATEGORIES.DINING.value),
                testExpense(name = "Train", price = 60.0, category = CATEGORIES.TRANSPORTATION.value),
                testExpense(name = "Lunch", price = 10.0, category = CATEGORIES.DINING.value)
            )
        )!!

        assertThat(stats.categories.map { it.category.id to it.total }).containsExactly(
            CATEGORIES.TRANSPORTATION.value to 60.0,
            CATEGORIES.DINING.value to 40.0
        ).inOrder()
        assertThat(stats.categories.sumOf { it.share }).isWithin(1e-9).of(1.0)
    }

    @Test
    fun categories_onATie_keepTheFixedCategoryOrder() {
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Train", price = 10.0, category = CATEGORIES.TRANSPORTATION.value),
                testExpense(name = "Rent", price = 10.0, category = CATEGORIES.HOUSING.value)
            )
        )!!

        assertThat(stats.categories.map { it.category.id }).containsExactly(CATEGORIES.HOUSING.value, CATEGORIES.TRANSPORTATION.value).inOrder()
    }
}
