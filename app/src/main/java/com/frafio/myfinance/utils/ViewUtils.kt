package com.frafio.myfinance.utils

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.frafio.myfinance.R
import com.google.android.material.color.DynamicColors
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries

fun View.instantShow() {
    visibility = View.VISIBLE
}

fun View.instantHide() {
    visibility = View.GONE
}

fun TextView.clearText() {
    text = ""
}

fun ValueLineChart.setValueLineChartData(list: List<Pair<String, Double>>, dynamicColors: Boolean) {
    ValueLineSeries().also { series ->
        val colorPrimaryContainer: Int
        if (dynamicColors) {
            // if your base context is already using Material3 theme you can omit R.style argument
            val dynamicColorContext = DynamicColors.wrapContextIfAvailable(
                context,
                R.style.ThemeOverlay_Material3_DynamicColors_DayNight
            )
            // define attributes to resolve in an array
            val attrsToResolve = intArrayOf(
                R.attr.colorPrimaryContainer, // 0
            )
            // now resolve them
            val ta = dynamicColorContext.obtainStyledAttributes(attrsToResolve)
            colorPrimaryContainer = ta.getColor(0, 0)
            ta.recycle() // recycle TypedArray
        } else {
            colorPrimaryContainer = ContextCompat.getColor(
                context,
                R.color.md_theme_primaryContainer
            )
        }
        series.color = ColorUtils
            .setAlphaComponent(
                colorPrimaryContainer, 200
            )

        list.forEach { pair ->
            val date: String = pair.first
            // convert to Float e round the the second decimal figure
            val value: Float = pair.second.toFloat().round(2)
            series.addPoint(ValueLinePoint(date, value))
        }

        addSeries(series)
    }
}