package com.frafio.charts

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
