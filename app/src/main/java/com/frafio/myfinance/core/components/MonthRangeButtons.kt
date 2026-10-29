package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.shownMonthRange

/**
 * The Auto / 6M / 1Y / 2Y buttons over a monthly chart. Only the [availableRanges] get a button,
 * Auto standing in for an unavailable [selectedRange]; Auto alone shows none.
 */
@Composable
fun MonthRangeButtons(
    selectedRange: Int,
    onSelectedRangeChange: (Int) -> Unit,
    availableRanges: List<Int>
) {
    if (availableRanges.size <= 1) return
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
