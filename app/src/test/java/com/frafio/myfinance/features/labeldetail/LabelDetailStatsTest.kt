package com.frafio.myfinance.features.labeldetail

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
    fun span_countsEveryDayFromTheFirstToTheLastExpense() {
        // A trip booked months ahead: two active days over 198.
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Train", date = LocalDate.of(2025, 8, 19)),
                testExpense(name = "Flight", date = LocalDate.of(2025, 2, 3))
            )
        )!!

        assertThat(stats.first).isEqualTo(LocalDate.of(2025, 2, 3))
        assertThat(stats.last).isEqualTo(LocalDate.of(2025, 8, 19))
        assertThat(stats.spanDays).isEqualTo(198)
        assertThat(stats.activeDays).isEqualTo(2)
    }

    @Test
    fun singleExpense_spansOneDay() {
        val stats = buildLabelDetail(listOf(testExpense(price = 5.0, date = aug12)))!!

        assertThat(stats.spanDays).isEqualTo(1)
        assertThat(stats.biggestDay).isEqualTo(DayTotal(aug12, 5.0))
    }

    @Test
    fun biggestDay_sumsTheDaysExpenses() {
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Dinner", price = 50.0, date = aug14),
                testExpense(name = "Lunch", price = 40.0, date = aug12),
                testExpense(name = "Museum", price = 20.0, date = aug12)
            )
        )!!

        assertThat(stats.biggestDay).isEqualTo(DayTotal(aug12, 60.0))
    }

    @Test
    fun biggestDay_onATie_isTheMostRecent() {
        val stats = buildLabelDetail(
            listOf(
                testExpense(name = "Dinner", price = 50.0, date = aug14),
                testExpense(name = "Lunch", price = 50.0, date = aug12)
            )
        )!!

        assertThat(stats.biggestDay).isEqualTo(DayTotal(aug14, 50.0))
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

    // region timeline

    private fun spentOn(vararg dates: LocalDate) = dates.map { testExpense(name = "E$it", price = 10.0, date = it) }

    @Test
    fun timeline_singleExpense_isOneDay() {
        val timeline = buildLabelTimeline(spentOn(aug12))

        assertThat(timeline).isEqualTo(LabelTimeline(TimelineUnit.DAY, listOf(TimelineSlot.Bucket(aug12, 10.0))))
    }

    @Test
    fun timeline_sumsEachDay_andKeepsShortEmptyRunsAsZeros() {
        val timeline = buildLabelTimeline(spentOn(aug12, aug12, aug14))

        assertThat(timeline.slots).containsExactly(
            TimelineSlot.Bucket(aug12, 20.0),
            TimelineSlot.Bucket(LocalDate.of(2025, 8, 13), 0.0),
            TimelineSlot.Bucket(aug14, 10.0)
        ).inOrder()
    }

    @Test
    fun timeline_sixEmptyDays_stayAsZeros_sevenCollapseIntoAGap() {
        val six = buildLabelTimeline(spentOn(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 8)))
        val seven = buildLabelTimeline(spentOn(LocalDate.of(2025, 8, 1), LocalDate.of(2025, 8, 9)))

        assertThat(six.slots).hasSize(8)
        assertThat(six.slots.none { it is TimelineSlot.Gap }).isTrue()
        assertThat(seven.slots).containsExactly(
            TimelineSlot.Bucket(LocalDate.of(2025, 8, 1), 10.0),
            TimelineSlot.Gap(LocalDate.of(2025, 8, 2), 7),
            TimelineSlot.Bucket(LocalDate.of(2025, 8, 9), 10.0)
        ).inOrder()
    }

    @Test
    fun timeline_tripBookedMonthsAhead_staysDaily_withItsGapsCollapsed() {
        val trip = (12..19).map { LocalDate.of(2025, 8, it) }.toTypedArray()
        val timeline = buildLabelTimeline(spentOn(LocalDate.of(2025, 2, 3), LocalDate.of(2025, 4, 10), LocalDate.of(2025, 7, 20), *trip))

        assertThat(timeline.unit).isEqualTo(TimelineUnit.DAY)
        assertThat(timeline.slots.filterIsInstance<TimelineSlot.Gap>().map { it.units }).containsExactly(65, 100, 22).inOrder()
        assertThat(timeline.slots).hasSize(14)
    }

    @Test
    fun timeline_aMonthOfDays_isDaily_oneMoreDayMovesToWeeks() {
        val days = (0L until 32L).map { aug12.plusDays(it) }

        assertThat(buildLabelTimeline(spentOn(*days.take(31).toTypedArray())).unit).isEqualTo(TimelineUnit.DAY)
        assertThat(buildLabelTimeline(spentOn(*days.toTypedArray())).unit).isEqualTo(TimelineUnit.WEEK)
    }

    @Test
    fun timeline_moreThanThreeGaps_movesToACoarserUnit() {
        // Every two weeks: four 13-day gaps by day, but only single empty weeks by week.
        val dates = (0L until 5L).map { LocalDate.of(2025, 1, 6).plusWeeks(2 * it) }

        val timeline = buildLabelTimeline(spentOn(*dates.toTypedArray()))

        assertThat(timeline.unit).isEqualTo(TimelineUnit.WEEK)
        assertThat(timeline.slots).hasSize(9)
        assertThat(timeline.slots.none { it is TimelineSlot.Gap }).isTrue()
    }

    @Test
    fun timeline_weeksStartOnMonday() {
        // Sunday 17 and Monday 18 August fall in different weeks.
        val weeks = timelineSlots(spentOn(LocalDate.of(2025, 8, 17), LocalDate.of(2025, 8, 18)), TimelineUnit.WEEK)

        assertThat(weeks.map { it.start }).containsExactly(LocalDate.of(2025, 8, 11), LocalDate.of(2025, 8, 18)).inOrder()
    }

    @Test
    fun timeline_aLongSparseLabel_isMonthly() {
        val dates = listOf(
            LocalDate.of(2024, 3, 2), LocalDate.of(2024, 5, 12), LocalDate.of(2024, 6, 3), LocalDate.of(2024, 9, 21),
            LocalDate.of(2024, 11, 5), LocalDate.of(2025, 1, 18), LocalDate.of(2025, 2, 11), LocalDate.of(2025, 4, 28)
        )

        val timeline = buildLabelTimeline(spentOn(*dates.toTypedArray()))

        assertThat(timeline.unit).isEqualTo(TimelineUnit.MONTH)
        assertThat(timeline.slots).hasSize(14)
        assertThat(timeline.slots.none { it is TimelineSlot.Gap }).isTrue()
    }

    @Test
    fun timeline_31Months_areMonthly_32MoveToYears_whereEmptyYearsStayAsZeros() {
        val months = (0L until 32L).map { LocalDate.of(2020, 1, 15).plusMonths(it) }

        assertThat(buildLabelTimeline(spentOn(*months.take(31).toTypedArray())).unit).isEqualTo(TimelineUnit.MONTH)
        val years = buildLabelTimeline(spentOn(*months.toTypedArray(), LocalDate.of(2026, 3, 1)))
        assertThat(years.unit).isEqualTo(TimelineUnit.YEAR)
        assertThat(years.slots.map { it.start.year }).containsExactly(2020, 2021, 2022, 2023, 2024, 2025, 2026).inOrder()
        assertThat(years.slots.none { it is TimelineSlot.Gap }).isTrue()
    }

    @Test
    fun gapLength_readsInTheUnitThatFitsIt() {
        val day = TimelineUnit.DAY
        val start = LocalDate.of(2025, 2, 4)

        assertThat(gapLength(TimelineSlot.Gap(start, 9), day)).isEqualTo(GapLength(9, GapUnit.DAYS))
        assertThat(gapLength(TimelineSlot.Gap(start, 22), day)).isEqualTo(GapLength(3, GapUnit.WEEKS))
        // 4 February to 10 April.
        assertThat(gapLength(TimelineSlot.Gap(start, 65), day)).isEqualTo(GapLength(2, GapUnit.MONTHS))
        assertThat(gapLength(TimelineSlot.Gap(LocalDate.of(2025, 1, 1), 5), TimelineUnit.MONTH)).isEqualTo(GapLength(5, GapUnit.MONTHS))
        assertThat(gapLength(TimelineSlot.Gap(LocalDate.of(2020, 1, 1), 30), TimelineUnit.MONTH)).isEqualTo(GapLength(2, GapUnit.YEARS))
    }

    @Test
    fun timelineKeys_growByOnePerUnit() {
        val monday = LocalDate.of(2025, 8, 11)

        assertThat(TimelineUnit.DAY.keyOf(monday.plusDays(1)) - TimelineUnit.DAY.keyOf(monday)).isEqualTo(1)
        assertThat(TimelineUnit.WEEK.keyOf(monday.plusWeeks(1)) - TimelineUnit.WEEK.keyOf(monday)).isEqualTo(1)
        assertThat(TimelineUnit.MONTH.keyOf(LocalDate.of(2025, 1, 1)) - TimelineUnit.MONTH.keyOf(LocalDate.of(2024, 12, 1))).isEqualTo(1)
        assertThat(TimelineUnit.YEAR.keyOf(LocalDate.of(2025, 1, 1))).isEqualTo(2025)
    }

    // endregion
}
