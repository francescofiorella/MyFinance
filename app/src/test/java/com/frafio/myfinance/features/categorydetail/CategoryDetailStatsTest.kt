package com.frafio.myfinance.features.categorydetail

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.SpendingChange
import com.frafio.myfinance.testing.data.testExpense
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class CategoryDetailStatsTest {

    private val dining = CATEGORIES.DINING.value

    // region buildCategoryDetail

    private val may2024 = LocalDate.of(2024, 5, 1)

    private fun expense(name: String, price: Double, year: Int, month: Int, day: Int = 10, labels: List<String> = emptyList()) =
        testExpense(name = name, price = price, date = LocalDate.of(year, month, day), category = dining, labels = labels, id = "$name-$year-$month-$day-$price")

    // The DAO returns the window newest first.
    private fun List<Expense>.newestFirst() =
        sortedWith(compareByDescending<Expense> { it.year }.thenByDescending { it.month }.thenByDescending { it.day })

    @Test
    fun buildCategoryDetail_trendCoversTwelveMonths_oldestFirst_withGapsAtZero() {
        val stats = buildCategoryDetail(
            month = may2024,
            windowExpenses = listOf(expense("A", 10.0, 2024, 5), expense("B", 20.0, 2023, 6)).newestFirst(),
            monthTotalAll = 100.0
        )

        assertThat(stats.trend).hasSize(TREND_MONTHS)
        assertThat(stats.trend.first().year to stats.trend.first().month).isEqualTo(2023 to 6)
        assertThat(stats.trend.last().year to stats.trend.last().month).isEqualTo(2024 to 5)
        assertThat(stats.trend.first().value).isEqualTo(20.0)
        assertThat(stats.trend.last().value).isEqualTo(10.0)
        assertThat(stats.trend.subList(1, TREND_MONTHS - 1).map { it.value }.distinct()).containsExactly(0.0)
    }

    @Test
    fun buildCategoryDetail_averageDividesTheWindowByTwelve_evenWithMissingMonths() {
        val stats = buildCategoryDetail(
            month = may2024,
            windowExpenses = listOf(expense("A", 60.0, 2024, 5), expense("B", 60.0, 2024, 1)).newestFirst(),
            monthTotalAll = 0.0
        )

        assertThat(stats.average).isEqualTo(10.0)
    }

    @Test
    fun buildCategoryDetail_yearToDate_onlyCountsTheSelectedYear() {
        val stats = buildCategoryDetail(
            month = may2024,
            windowExpenses = listOf(
                expense("A", 1.0, 2024, 5),
                expense("B", 2.0, 2024, 1),
                expense("C", 4.0, 2023, 12),
                expense("D", 8.0, 2023, 6),
            ).newestFirst(),
            monthTotalAll = 0.0
        )

        assertThat(stats.yearToDate).isEqualTo(3.0)
    }

    @Test
    fun buildCategoryDetail_monthTotalShareAndChange() {
        val stats = buildCategoryDetail(
            month = may2024,
            windowExpenses = listOf(
                expense("A", 30.0, 2024, 5),
                expense("B", 20.0, 2024, 5, day = 2),
                expense("C", 40.0, 2024, 4),
            ).newestFirst(),
            monthTotalAll = 200.0
        )

        assertThat(stats.monthTotal).isEqualTo(50.0)
        assertThat(stats.share).isEqualTo(0.25)
        assertThat(stats.change).isEqualTo(SpendingChange.Percent(0.25))
    }

    @Test
    fun buildCategoryDetail_shareIsZero_whenNothingWasSpentInTheMonth() {
        val stats = buildCategoryDetail(may2024, emptyList(), monthTotalAll = 0.0)

        assertThat(stats.share).isEqualTo(0.0)
        assertThat(stats.change).isEqualTo(SpendingChange.None)
        assertThat(stats.topExpenses).isEmpty()
    }

    @Test
    fun buildCategoryDetail_topExpenses_areTheMonthsBiggestFive() {
        val thisMonth = (1..7).map { expense("E$it", it * 10.0, 2024, 5, day = it) }
        val stats = buildCategoryDetail(
            month = may2024,
            windowExpenses = (thisMonth + expense("Older", 999.0, 2024, 4)).newestFirst(),
            monthTotalAll = 0.0
        )

        assertThat(stats.topExpenses.map { it.name }).containsExactly("E7", "E6", "E5", "E4", "E3").inOrder()
    }

    // endregion

    // region frequentExpenses

    @Test
    fun frequentExpenses_matchesIgnoringCaseAndSpaces_andKeepsTheLatestSpelling() {
        val frequent = frequentExpenses(
            listOf(
                expense("Sushi Bar", 30.0, 2024, 5),
                expense(" sushi bar ", 20.0, 2024, 4),
                expense("SUSHI BAR", 10.0, 2024, 3),
            ).newestFirst()
        )

        assertThat(frequent).containsExactly(FrequentExpense("Sushi Bar", 3, 60.0))
    }

    @Test
    fun frequentExpenses_needsAtLeastTwo_sortsByCountThenTotal_andKeepsThree() {
        val frequent = frequentExpenses(
            listOf(
                expense("Once", 500.0, 2024, 5),
                expense("Coffee", 2.0, 2024, 5, day = 1), expense("Coffee", 2.0, 2024, 5, day = 2), expense("Coffee", 2.0, 2024, 5, day = 3),
                expense("Pizza", 15.0, 2024, 5, day = 4), expense("Pizza", 15.0, 2024, 5, day = 5),
                expense("Kebab", 8.0, 2024, 5, day = 6), expense("Kebab", 8.0, 2024, 5, day = 7),
                expense("Bar", 1.0, 2024, 5, day = 8), expense("Bar", 1.0, 2024, 5, day = 9),
            ).newestFirst()
        )

        assertThat(frequent.map { it.name }).containsExactly("Coffee", "Pizza", "Kebab").inOrder()
    }

    // endregion

    // region labelSpending

    @Test
    fun labelSpending_countsAnExpenseTowardEachOfItsLabels_highestFirst() {
        val labels = labelSpending(
            listOf(
                expense("A", 10.0, 2024, 5, labels = listOf("Dinner", "Friends")),
                expense("B", 30.0, 2024, 5, labels = listOf("Dinner")),
                expense("C", 5.0, 2024, 5),
                // A label repeated on one expense still counts once.
                expense("D", 1.0, 2024, 5, labels = listOf("Work", "Work")),
            )
        )

        assertThat(labels).containsExactly(
            LabelSpending("Dinner", 40.0),
            LabelSpending("Friends", 10.0),
            LabelSpending("Work", 1.0),
        ).inOrder()
    }

    // endregion
}
