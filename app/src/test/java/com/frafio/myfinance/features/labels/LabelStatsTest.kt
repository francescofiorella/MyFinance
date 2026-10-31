package com.frafio.myfinance.features.labels

import com.frafio.myfinance.testing.data.testExpense
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class LabelStatsTest {

    @Test
    fun summary_addsUpTheLabelsExpenses_andSpansFirstToLastDay() {
        val groups = buildLabelGroups(
            labels = listOf("Japan"),
            expenses = listOf(
                testExpense(name = "Hotel", price = 300.0, date = LocalDate.of(2025, 8, 12), labels = listOf("Japan")),
                testExpense(name = "Flight", price = 900.0, date = LocalDate.of(2025, 2, 3), labels = listOf("Japan")),
                testExpense(name = "Coffee", price = 2.0, date = LocalDate.of(2025, 3, 1))
            )
        )

        assertThat(groups).containsExactly(
            LabelGroup(2025, listOf(LabelSummary("Japan", 1200.0, 2, LocalDate.of(2025, 2, 3), LocalDate.of(2025, 8, 12))))
        )
    }

    @Test
    fun anExpenseWithSeveralLabels_countsForEach() {
        val dinner = testExpense(name = "Dinner", price = 50.0, date = LocalDate.of(2025, 8, 13), labels = listOf("Japan", "Work"))

        val summaries = buildLabelGroups(listOf("Japan", "Work"), listOf(dinner)).single().summaries

        assertThat(summaries.map { it.label to it.total }).containsExactly("Japan" to 50.0, "Work" to 50.0)
    }

    @Test
    fun aLabelRepeatedOnOneExpense_countsItOnce() {
        val dinner = testExpense(price = 50.0, labels = listOf("Japan", "Japan"))

        val summary = buildLabelGroups(listOf("Japan"), listOf(dinner)).single().summaries.single()

        assertThat(summary.count).isEqualTo(1)
        assertThat(summary.total).isEqualTo(50.0)
    }

    @Test
    fun labelsNotInTheList_areIgnored() {
        val stale = testExpense(labels = listOf("Removed"))

        assertThat(buildLabelGroups(listOf("Japan"), listOf(stale)))
            .containsExactly(LabelGroup(null, listOf(LabelSummary("Japan", 0.0, 0, null, null))))
    }

    @Test
    fun groups_followTheYearOfTheLastExpense_newestFirst_mostRecentFirstInside_thenTheUnused() {
        val groups = buildLabelGroups(
            labels = listOf("Christmas", "Japan", "Lisbon", "Paris", "Renovation"),
            expenses = listOf(
                // Started in 2024 but last used in 2025, so it belongs to 2025.
                testExpense(name = "Tiles", date = LocalDate.of(2024, 3, 2), labels = listOf("Renovation")),
                testExpense(name = "Paint", date = LocalDate.of(2025, 4, 28), labels = listOf("Renovation")),
                testExpense(name = "Hotel", date = LocalDate.of(2025, 8, 12), labels = listOf("Japan")),
                testExpense(name = "Tram", date = LocalDate.of(2024, 9, 16), labels = listOf("Lisbon")),
                testExpense(name = "Gifts", date = LocalDate.of(2024, 12, 24), labels = listOf("Christmas"))
            )
        )

        assertThat(groups.map { group -> group.year to group.summaries.map { it.label } }).containsExactly(
            2025 to listOf("Japan", "Renovation"),
            2024 to listOf("Christmas", "Lisbon"),
            null to listOf("Paris")
        ).inOrder()
    }

    @Test
    fun labelsLastUsedOnTheSameDay_areSortedByName() {
        val day = LocalDate.of(2025, 5, 10)
        val groups = buildLabelGroups(
            labels = listOf("Wedding", "Gifts"),
            expenses = listOf(testExpense(date = day, labels = listOf("Wedding", "Gifts")))
        )

        assertThat(groups.single().summaries.map { it.label }).containsExactly("Gifts", "Wedding").inOrder()
    }

    @Test
    fun noLabels_isEmpty() {
        assertThat(buildLabelGroups(emptyList(), listOf(testExpense(labels = listOf("Japan"))))).isEmpty()
    }
}
