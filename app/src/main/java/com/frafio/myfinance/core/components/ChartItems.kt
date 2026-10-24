package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.frafio.myfinance.core.data.model.CategoryColors
import com.frafio.myfinance.core.data.model.MonthlyTotal
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/** A pie chart slice in a category's [colors] for the current theme. */
@Composable
fun categoryPieChartItem(value: Double, label: String, @DrawableRes icon: Int, colors: CategoryColors): PieChartItem {
    val isDark = isSystemInDarkTheme()
    return PieChartItem(
        value = value,
        label = label,
        icon = painterResource(icon),
        color = colors.container(isDark),
        iconColor = colors.onContainer(isDark),
        labelColor = colors.text(isDark)
    )
}

/** The month's bar: keyed by months since year zero, labelled "MMMM yyyy", "MM" on the axis and "MM/yy" once selected. */
fun MonthlyTotal.toBarChartEntry(): BarChartEntry {
    val axisLabel = "%02d".format(month).takeLast(2)
    return BarChartEntry(
        key = year * 12 + month - 1,
        value = value,
        label = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy")),
        axisLabel = axisLabel,
        selectedAxisLabel = axisLabel + "/" + "%02d".format(year).takeLast(2)
    )
}
