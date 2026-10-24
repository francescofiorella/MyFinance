package com.frafio.myfinance.core.components

import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** How the app's monthly totals become chart bars; the test JVM runs in en-US. */
class ChartItemsTest {

    @Test
    fun toBarChartEntry_labelsTheMonth() {
        val entry = MonthlyTotal(value = 120.0, year = 2024, month = 12).toBarChartEntry()

        assertThat(entry.value).isEqualTo(120.0)
        assertThat(entry.label).isEqualTo("December 2024")
        assertThat(entry.axisLabel).isEqualTo("12")
        assertThat(entry.selectedAxisLabel).isEqualTo("12/24")
    }

    @Test
    fun toBarChartEntry_padsSingleDigitMonthsAndYears() {
        val entry = MonthlyTotal(value = 1.0, year = 2005, month = 3).toBarChartEntry()

        assertThat(entry.axisLabel).isEqualTo("03")
        assertThat(entry.selectedAxisLabel).isEqualTo("03/05")
    }

    @Test
    fun toBarChartEntry_keysAreConsecutiveMonths_acrossTheYear() {
        val december = MonthlyTotal(value = 0.0, year = 2023, month = 12).toBarChartEntry()
        val january = MonthlyTotal(value = 0.0, year = 2024, month = 1).toBarChartEntry()
        val march = MonthlyTotal(value = 0.0, year = 2024, month = 3).toBarChartEntry()

        assertThat(january.key - december.key).isEqualTo(1)
        assertThat(march.key - january.key).isEqualTo(2)
    }
}
