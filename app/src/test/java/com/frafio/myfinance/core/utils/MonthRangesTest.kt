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
}
