package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.frafio.myfinance.core.data.model.CategoryColors

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
