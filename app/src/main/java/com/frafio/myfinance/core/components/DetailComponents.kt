package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A card of a detail page's stats grid: [label] over a large [value], then any [extra] content. */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    extra: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            extra()
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** A row of a segmented group: [title] and an optional [subtitle] on the left, [value] on the right. */
@Composable
fun DetailRow(
    title: String,
    subtitle: String?,
    value: String,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    // Null keeps the row static.
    onClick: (() -> Unit)? = null
) {
    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val shapes = ListItemDefaults.segmentedShapes(
        index = index,
        count = count,
        defaultShapes = ListItemDefaults.shapes()
    )
    val rowModifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = if (index == count - 1) 0.dp else 2.dp)
    val content: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (onClick != null) {
        SegmentedListItem(onClick = onClick, colors = colors, shapes = shapes, content = content, modifier = rowModifier)
    } else {
        SegmentedListItem(colors = colors, shapes = shapes, content = content, modifier = rowModifier)
    }
}
