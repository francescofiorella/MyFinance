package com.frafio.charts.internal

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** The average of the [values] with data, null if none has. */
internal fun averageOf(values: List<Double?>): Double? = values.filterNotNull().takeIf { it.isNotEmpty() }?.average()

/**
 * The header of a chart with a selection: the selected entry's [selectedValue] and [selectedLabel], or with
 * nothing selected ([selectedLabel] null) the [average], labelled [averageLabel].
 */
@Composable
internal fun SelectionHeader(
    selectedValue: Double?,
    selectedLabel: String?,
    average: Double?,
    averageLabel: String,
    valueFormatter: (Double) -> String,
    color: Color
) {
    if (selectedLabel != null) {
        ChartHeader(value = selectedValue?.let(valueFormatter) ?: "", label = selectedLabel, color = color)
    } else {
        ChartHeader(value = average?.let(valueFormatter) ?: "", label = averageLabel, color = color)
    }
}

/** A large [value] with a small [label] beside it, above a chart. */
@Composable
internal fun ChartHeader(value: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
