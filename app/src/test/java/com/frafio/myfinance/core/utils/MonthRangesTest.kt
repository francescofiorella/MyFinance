package com.frafio.myfinance.core.utils

import com.frafio.myfinance.core.data.model.DatePoint
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class MonthRangesTest {

    @Test
    fun monthRangeCount_auto_isWhatFitsTheWidth() {
        assertThat(monthRangeCount(0, fittingBars = 7)).isEqualTo(7)
    }

    @Test
    fun monthRangeCount_fixedRanges_ignoreTheWidth() {
        assertThat((1..3).map { monthRangeCount(it, fittingBars = 7) }).containsExactly(6, 12, 24).inOrder()
    }

    @Test
    fun monthsToLoad_isTheLongestRange_unlessMoreFit() {
        assertThat(monthsToLoad(7)).isEqualTo(24)
        assertThat(monthsToLoad(30)).isEqualTo(30)
    }

    private val may2024 = LocalDate.of(2024, 5, 1)

    @Test
    fun trackingStart_isTheEarliestExpensesMonth() {
        assertThat(trackingStart(DatePoint(2023, 11), lastMonth = may2024)).isEqualTo(LocalDate.of(2023, 11, 1))
    }

    @Test
    fun trackingStart_withoutExpenses_isTheLastMonth() {
        assertThat(trackingStart(null, lastMonth = may2024)).isEqualTo(may2024)
    }

    @Test
    fun trackingStart_isNeverAfterTheLastMonth() {
        assertThat(trackingStart(DatePoint(2024, 8), lastMonth = may2024)).isEqualTo(may2024)
        assertThat(trackingStart(DatePoint(2024, 5), lastMonth = may2024)).isEqualTo(may2024)
    }

    @Test
    fun trackedMonths_countsBothEnds_acrossYears() {
        assertThat(trackedMonths(LocalDate.of(2023, 11, 1), lastMonth = may2024)).isEqualTo(7)
        assertThat(trackedMonths(may2024, lastMonth = may2024)).isEqualTo(1)
    }

    @Test
    fun trackedMonths_isAtLeastOne() {
        assertThat(trackedMonths(LocalDate.of(2024, 8, 1), lastMonth = may2024)).isEqualTo(1)
    }

    @Test
    fun availableMonthRanges_withAFewMonths_thatAutoShows_isOnlyAuto() {
        assertThat(availableMonthRanges(trackedMonths = 1, fittingBars = 7)).containsExactly(0)
        assertThat(availableMonthRanges(trackedMonths = 5, fittingBars = 7)).containsExactly(0)
        // 6M would show the same six months.
        assertThat(availableMonthRanges(trackedMonths = 6, fittingBars = 7)).containsExactly(0)
    }

    @Test
    fun availableMonthRanges_withAFewMonths_thatAutoCantShow_keeps6M() {
        assertThat(availableMonthRanges(trackedMonths = 5, fittingBars = 4)).containsExactly(0, 1).inOrder()
    }

    @Test
    fun availableMonthRanges_offersEachRange_whileTheShorterOneLeavesMonthsOut() {
        assertThat(availableMonthRanges(trackedMonths = 7, fittingBars = 7)).containsExactly(0, 1, 2).inOrder()
        assertThat(availableMonthRanges(trackedMonths = 10, fittingBars = 7)).containsExactly(0, 1, 2).inOrder()
        assertThat(availableMonthRanges(trackedMonths = 12, fittingBars = 7)).containsExactly(0, 1, 2).inOrder()
        assertThat(availableMonthRanges(trackedMonths = 13, fittingBars = 7)).containsExactly(0, 1, 2, 3).inOrder()
        assertThat(availableMonthRanges(trackedMonths = 40, fittingBars = 7)).containsExactly(0, 1, 2, 3).inOrder()
    }

    @Test
    fun shownMonthRange_fallsBackToAuto_whenTheSelectedOneIsUnavailable() {
        assertThat(shownMonthRange(2, available = listOf(0, 1, 2))).isEqualTo(2)
        assertThat(shownMonthRange(3, available = listOf(0, 1, 2))).isEqualTo(0)
    }
}
