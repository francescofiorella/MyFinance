package com.frafio.charts

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import kotlin.math.roundToInt

/**
 * A point of a [LineChart]. [key] orders the points and identifies one across entry changes; keys need
 * not be consecutive. [label] is read by accessibility services and [axisLabel] is shown under the point.
 * A null [value] means no data: the slot and its axis label stay, but no point is drawn and the line breaks.
 */
data class LineChartEntry(
    val key: Int,
    val value: Double?,
    val label: String,
    val axisLabel: String
)

/** The colors of a [LineChart]: [labelColor] for the axis labels. */
@Immutable
data class LineChartColors(
    val lineColor: Color,
    val pointColor: Color,
    val labelColor: Color
)

object LineChartDefaults {
    val PointSize: Dp = 8.dp
    val LineWidth: Dp = 2.dp

    /** The narrowest slot a point gets, used to report how many entries fit. */
    val PointSpacing: Dp = 46.dp
    val PlotHeight: Dp = 160.dp

    /** Formats the points' descriptions, in the default locale. */
    val ValueFormatter: (Double) -> String = { NumberFormat.getInstance().format(it) }

    /** How the strip scrolls, zooms and rescales when the entries change; pass it as `entryAnimationSpec`. */
    val EntryAnimationSpec: FiniteAnimationSpec<Float>
        @Composable get() = MaterialTheme.motionScheme.slowSpatialSpec()

    @Composable
    fun colors(
        lineColor: Color = MaterialTheme.colorScheme.primary,
        pointColor: Color = MaterialTheme.colorScheme.primary,
        labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ): LineChartColors = LineChartColors(lineColor, pointColor, labelColor)
}

/**
 * Each entry as a point, scaled on the largest value, joined to its neighbours by a line. With an
 * [entryAnimationSpec], entry changes animate as one strip matched by key: the shown keys scroll and
 * zoom to the new ones, so added points slide in and removed ones slide out at the edges.
 */
@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    entries: List<LineChartEntry>,
    onVisibleCountChanged: (Int) -> Unit = {},
    valueFormatter: (Double) -> String = LineChartDefaults.ValueFormatter,
    colors: LineChartColors = LineChartDefaults.colors(),
    pointSize: Dp = LineChartDefaults.PointSize,
    lineWidth: Dp = LineChartDefaults.LineWidth,
    pointSpacing: Dp = LineChartDefaults.PointSpacing,
    plotHeight: Dp = LineChartDefaults.PlotHeight,
    entryAnimationSpec: FiniteAnimationSpec<Float>? = null
) {
    BoxWithConstraints(modifier = modifier) {
        val maxVisibleItems = maxVisibleBars(maxWidth, pointSpacing, 0.dp)

        LaunchedEffect(maxVisibleItems) {
            onVisibleCountChanged(maxVisibleItems)
        }

        val maxValue = remember(entries) {
            val max = entries.mapNotNull { it.value }.maxOrNull() ?: 0.0
            if (max == 0.0) 1.0 else max
        }
        val strip = rememberEntryStrip(entries, maxValue, LineChartEntry::key, LineChartEntry::value, entryAnimationSpec)
        val viewportWidth = maxWidth

        Column(modifier = Modifier.fillMaxWidth()) {
            LineChartPlot(
                strip = strip,
                isAnimated = entryAnimationSpec != null,
                valueFormatter = valueFormatter,
                colors = colors,
                pointSize = pointSize,
                lineWidth = lineWidth,
                plotHeight = plotHeight
            )
            Spacer(modifier = Modifier.height(4.dp))
            StripAxis(
                strip = strip,
                selectedIndex = -1,
                viewportWidth = viewportWidth,
                label = { entry, _ -> entry.axisLabel },
                color = colors.labelColor,
                selectedColor = colors.labelColor
            )
        }
    }
}

@Composable
private fun LineChartPlot(
    strip: EntryStrip<LineChartEntry>,
    isAnimated: Boolean,
    valueFormatter: (Double) -> String,
    colors: LineChartColors,
    pointSize: Dp,
    lineWidth: Dp,
    plotHeight: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(plotHeight)
            .then(if (isAnimated) Modifier.clipToBounds() else Modifier)
    ) {
        Box(
            modifier = Modifier
                .stripWindow(strip)
                .fillMaxSize()
        ) {
            // Fractions are read while drawing and placing, so animation frames don't recompose.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val items = strip.items
                if (items.isEmpty()) return@Canvas
                val slot = size.width / items.size
                val radius = pointSize.toPx() / 2
                fun center(i: Int) = Offset((i + 0.5f) * slot, radius + (1f - items[i].fraction()) * (size.height - 2 * radius))
                for (i in 1 until items.size) {
                    // Entries without data break the line.
                    if (items[i - 1].entry.value == null || items[i].entry.value == null) continue
                    drawLine(colors.lineColor, center(i - 1), center(i), lineWidth.toPx(), StrokeCap.Round)
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                strip.items.forEach { item ->
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        val value = item.entry.value
                        if (value != null) {
                            val description = stringResource(R.string.charts_item_description, item.entry.label, valueFormatter(value))
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset { IntOffset(0, ((1f - item.fraction()) * (plotHeight - pointSize).toPx()).roundToInt()) }
                                    .size(pointSize)
                                    .background(colors.pointColor, CircleShape)
                                    .semantics { contentDescription = description }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun previewEntries(count: Int): List<LineChartEntry> = (0 until count).map {
    val month = "%02d".format(it % 12 + 1)
    val year = 2023 + it / 12
    LineChartEntry(
        key = it,
        // The first two months have no data, the fifth is a real zero.
        value = when (it) {
            0, 1 -> null
            4 -> 0.0
            else -> 60.0 + (it * 37) % 190
        },
        label = "$month/$year",
        axisLabel = month
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun LineChartPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            LineChart(entries = previewEntries(12))
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun LineChartManyEntriesPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            LineChart(entries = previewEntries(24), entryAnimationSpec = LineChartDefaults.EntryAnimationSpec)
        }
    }
}
