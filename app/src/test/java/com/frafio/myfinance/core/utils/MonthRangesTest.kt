package com.frafio.myfinance.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

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
}
