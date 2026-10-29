package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.frafio.charts.LineChart
import com.frafio.charts.LineChartColors
import com.frafio.charts.LineChartDefaults
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.core.utils.doubleToPrice
import java.time.LocalDate

/**
 * The Auto / 6M / 1Y / 2Y buttons over a line chart of the last [shownCount] of [totals]; changing range
 * slides the points. Months before [trackingStart] are shown as no data; null treats every month as data.
 */
@Composable
fun MonthlyRangeLineChart(
    totals: List<MonthlyTotal>,
    shownCount: Int,
    selectedRange: Int,
    onSelectedRangeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableRanges: List<Int> = ALL_MONTH_RANGES,
    trackingStart: LocalDate? = null,
    onVisibleCountChanged: (Int) -> Unit = {},
    colors: LineChartColors = LineChartDefaults.colors()
) {
    val shown = totals.takeLast(shownCount)

    Column(modifier = modifier.fillMaxWidth()) {
        MonthRangeButtons(selectedRange = selectedRange, onSelectedRangeChange = onSelectedRangeChange, availableRanges = availableRanges)

        LineChart(
            entries = remember(shown, trackingStart) {
                shown.map { it.toLineChartEntry(hasData = trackingStart == null || !LocalDate.of(it.year, it.month, 1).isBefore(trackingStart)) }
            },
            onVisibleCountChanged = onVisibleCountChanged,
            valueFormatter = ::doubleToPrice,
            colors = colors,
            entryAnimationSpec = LineChartDefaults.EntryAnimationSpec
        )
    }
}
