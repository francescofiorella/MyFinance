package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.frafio.charts.BarChart
import com.frafio.charts.BarChartDefaults
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.core.utils.shownMonthRange
import java.time.LocalDate

/**
 * The Auto / 6M / 1Y / 2Y buttons over a bar chart of the last [shownCount] of [totals]; changing range
 * slides the bars. [selectedMonth] indexes [totals], -1 being the latest, and goes back to -1 once the
 * range no longer shows it. Months before [trackingStart] are shown as no data; null treats every month as data.
 * Only the [availableRanges] get a button, Auto standing in for an unavailable [selectedRange]; Auto alone shows none.
 */
@Composable
fun MonthlyRangeBarChart(
    totals: List<MonthlyTotal>,
    shownCount: Int,
    selectedRange: Int,
    onSelectedRangeChange: (Int) -> Unit,
    selectedMonth: Int,
    onSelectedMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableRanges: List<Int> = ALL_MONTH_RANGES,
    trackingStart: LocalDate? = null,
    referenceValue: Double? = null,
    onVisibleCountChanged: (Int) -> Unit = {}
) {
    val shown = totals.takeLast(shownCount)
    val firstShown = totals.size - shown.size

    LaunchedEffect(firstShown) {
        if (selectedMonth in 0 until firstShown) onSelectedMonthChange(-1)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (availableRanges.size > 1) {
            val labels = listOf(
                stringResource(R.string.bar_chart_range_dynamic),
                stringResource(R.string.bar_chart_range_6_months),
                stringResource(R.string.bar_chart_range_1_year),
                stringResource(R.string.bar_chart_range_2_years)
            )
            ConnectedToggleRow(
                modifier = Modifier.padding(bottom = 24.dp),
                options = availableRanges.map { labels[it] },
                selectedIndex = availableRanges.indexOf(shownMonthRange(selectedRange, availableRanges)),
                onSelect = { onSelectedRangeChange(availableRanges[it]) }
            )
        }

        BarChart(
            entries = remember(shown, trackingStart) {
                shown.map { it.toBarChartEntry(hasData = trackingStart == null || !LocalDate.of(it.year, it.month, 1).isBefore(trackingStart)) }
            },
            selectedIndex = if (selectedMonth >= firstShown) selectedMonth - firstShown else -1,
            onSelectedIndexChange = { onSelectedMonthChange(it + firstShown) },
            referenceValue = referenceValue,
            onVisibleCountChanged = onVisibleCountChanged,
            valueFormatter = ::doubleToPrice,
            referenceValueFormatter = ::doubleToPriceWithoutDecimals,
            entryAnimationSpec = BarChartDefaults.EntryAnimationSpec
        )
    }
}
