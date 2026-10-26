package com.frafio.myfinance.core.components

import androidx.compose.animation.core.tween
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
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals

/**
 * The Auto / 6M / 1Y / 2Y buttons over a bar chart of the last [shownCount] of [totals]; changing range
 * slides the bars. [selectedMonth] indexes [totals], -1 being the latest, and goes back to -1 once the
 * range no longer shows it.
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
    referenceValue: Double? = null,
    onVisibleCountChanged: (Int) -> Unit = {}
) {
    val shown = totals.takeLast(shownCount)
    val firstShown = totals.size - shown.size

    LaunchedEffect(firstShown) {
        if (selectedMonth in 0 until firstShown) onSelectedMonthChange(-1)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        ConnectedToggleRow(
            modifier = Modifier.padding(bottom = 16.dp),
            options = listOf(
                stringResource(R.string.bar_chart_range_dynamic),
                stringResource(R.string.bar_chart_range_6_months),
                stringResource(R.string.bar_chart_range_1_year),
                stringResource(R.string.bar_chart_range_2_years)
            ),
            selectedIndex = selectedRange,
            onSelect = onSelectedRangeChange
        )

        BarChart(
            entries = remember(shown) { shown.map { it.toBarChartEntry() } },
            selectedIndex = if (selectedMonth >= firstShown) selectedMonth - firstShown else -1,
            onSelectedIndexChange = { onSelectedMonthChange(it + firstShown) },
            referenceValue = referenceValue,
            onVisibleCountChanged = onVisibleCountChanged,
            valueFormatter = ::doubleToPrice,
            referenceValueFormatter = ::doubleToPriceWithoutDecimals,
            entryAnimationSpec = tween(durationMillis = 500)
        )
    }
}
