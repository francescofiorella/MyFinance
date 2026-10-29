package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.graphics.shapes.RoundedPolygon
import com.frafio.charts.LineChart
import com.frafio.charts.LineChartColors
import com.frafio.charts.LineChartDefaults
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.core.utils.doubleToPrice
import java.time.LocalDate

/**
 * The Auto / 6M / 1Y / 2Y buttons over a line chart of the last [shownCount] of [totals]; changing range
 * slides the points. [selectedMonth] indexes [totals], -1 being none, and goes back to -1 once the range no
 * longer shows it; with none selected the header shows the average of the shown months with data. The
 * selected point morphs into [selectedPointShape]. Months before [trackingStart] are shown as no data; null
 * treats every month as data.
 */
@Composable
fun MonthlyRangeLineChart(
    totals: List<MonthlyTotal>,
    shownCount: Int,
    selectedRange: Int,
    onSelectedRangeChange: (Int) -> Unit,
    selectedMonth: Int,
    onSelectedMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableRanges: List<Int> = ALL_MONTH_RANGES,
    trackingStart: LocalDate? = null,
    onVisibleCountChanged: (Int) -> Unit = {},
    colors: LineChartColors = LineChartDefaults.colors(),
    selectedPointShape: RoundedPolygon = LineChartDefaults.SelectedPointShape
) {
    val shown = totals.takeLast(shownCount)
    val firstShown = totals.size - shown.size

    LaunchedEffect(firstShown) {
        if (selectedMonth in 0 until firstShown) onSelectedMonthChange(-1)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        MonthRangeButtons(selectedRange = selectedRange, onSelectedRangeChange = onSelectedRangeChange, availableRanges = availableRanges)

        val entries = remember(shown, trackingStart) {
            shown.map { it.toLineChartEntry(hasData = trackingStart == null || !LocalDate.of(it.year, it.month, 1).isBefore(trackingStart)) }
        }
        // The average covers only the months with data.
        val trackedMonths = entries.count { it.value != null }
        LineChart(
            entries = entries,
            selectedIndex = if (selectedMonth >= firstShown) selectedMonth - firstShown else -1,
            onSelectedIndexChange = { onSelectedMonthChange(if (it >= 0) it + firstShown else -1) },
            averageLabel = pluralStringResource(R.plurals.trend_average_months, trackedMonths, trackedMonths),
            onVisibleCountChanged = onVisibleCountChanged,
            valueFormatter = ::doubleToPrice,
            colors = colors,
            selectedPointShape = selectedPointShape,
            entryAnimationSpec = LineChartDefaults.EntryAnimationSpec
        )
    }
}
