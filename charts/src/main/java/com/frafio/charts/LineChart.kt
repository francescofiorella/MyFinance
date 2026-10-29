package com.frafio.charts

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import kotlin.math.roundToInt

/**
 * A point of a [LineChart]. [key] orders the points and identifies one across entry changes; keys need
 * not be consecutive. [label] is read by accessibility services, [axisLabel] is shown under the point and
 * [selectedAxisLabel] under the latest one.
 * A null [value] means no data: the slot and its axis label stay, but no point is drawn and the line breaks.
 */
data class LineChartEntry(
    val key: Int,
    val value: Double?,
    val label: String,
    val axisLabel: String,
    val selectedAxisLabel: String = axisLabel
)

/** The colors of a [LineChart]: [labelColor] for the axis labels, [contentColor] for the header and the selected axis label. */
@Immutable
data class LineChartColors(
    val lineColor: Color,
    val pointColor: Color,
    val labelColor: Color,
    val contentColor: Color
)

object LineChartDefaults {
    val PointSize: Dp = 8.dp
    val LineWidth: Dp = 2.dp

    /** The space left between a point and the line reaching it. */
    val PointGap: Dp = 2.dp

    /** The narrowest slot a point gets, used to report how many entries fit. */
    val PointSpacing: Dp = 46.dp
    val PlotHeight: Dp = 160.dp

    /** Formats the header value or average and the points' descriptions, in the default locale. */
    val ValueFormatter: (Double) -> String = { NumberFormat.getInstance().format(it) }

    /** How the strip scrolls, zooms and rescales when the entries change; pass it as `entryAnimationSpec`. */
    val EntryAnimationSpec: FiniteAnimationSpec<Float>
        @Composable get() = MaterialTheme.motionScheme.slowSpatialSpec()

    @Composable
    fun colors(
        lineColor: Color = MaterialTheme.colorScheme.primary,
        pointColor: Color = MaterialTheme.colorScheme.primary,
        labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        contentColor: Color = MaterialTheme.colorScheme.onSurface
    ): LineChartColors = LineChartColors(lineColor, pointColor, labelColor, contentColor)
}

/**
 * A line chart that keeps its own selection, starting with none. [onPointClick] reports the selected point,
 * or -1 once tapping it again clears the selection. With an [entryAnimationSpec], entry changes animate as
 * one strip matched by key.
 */
@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    entries: List<LineChartEntry>,
    averageLabel: String = "",
    onPointClick: (Int) -> Unit = {},
    onVisibleCountChanged: (Int) -> Unit = {},
    valueFormatter: (Double) -> String = LineChartDefaults.ValueFormatter,
    colors: LineChartColors = LineChartDefaults.colors(),
    pointSize: Dp = LineChartDefaults.PointSize,
    lineWidth: Dp = LineChartDefaults.LineWidth,
    pointGap: Dp = LineChartDefaults.PointGap,
    pointSpacing: Dp = LineChartDefaults.PointSpacing,
    plotHeight: Dp = LineChartDefaults.PlotHeight,
    entryAnimationSpec: FiniteAnimationSpec<Float>? = null
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    LineChart(
        modifier = modifier,
        entries = entries,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = {
            selectedIndex = it
            onPointClick(it)
        },
        averageLabel = averageLabel,
        onVisibleCountChanged = onVisibleCountChanged,
        valueFormatter = valueFormatter,
        colors = colors,
        pointSize = pointSize,
        lineWidth = lineWidth,
        pointGap = pointGap,
        pointSpacing = pointSpacing,
        plotHeight = plotHeight,
        entryAnimationSpec = entryAnimationSpec
    )
}

/**
 * Each entry as a point, scaled on the largest value, joined to its neighbours by a line, with a hoisted
 * selection. The header shows the selected entry, or with nothing selected the average of the entries with
 * data, labelled [averageLabel]. A [selectedIndex] outside [entries] (e.g. -1) or on an entry without data
 * selects nothing; tapping anywhere in a point's slot selects it, and tapping the selected one clears the
 * selection, reporting -1. With an [entryAnimationSpec], entry changes animate as one strip matched by key:
 * the shown keys scroll and zoom to the new ones, so added points slide in and removed ones slide out at the edges.
 */
@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    entries: List<LineChartEntry>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    averageLabel: String = "",
    onVisibleCountChanged: (Int) -> Unit = {},
    valueFormatter: (Double) -> String = LineChartDefaults.ValueFormatter,
    colors: LineChartColors = LineChartDefaults.colors(),
    pointSize: Dp = LineChartDefaults.PointSize,
    lineWidth: Dp = LineChartDefaults.LineWidth,
    pointGap: Dp = LineChartDefaults.PointGap,
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
        val shownIndex = shownSelection(entries.map { it.value }, selectedIndex)
        val strip = rememberEntryStrip(entries, maxValue, LineChartEntry::key, LineChartEntry::value, entryAnimationSpec)
        val viewportWidth = maxWidth

        Column(modifier = Modifier.fillMaxWidth()) {
            val entry = entries.getOrNull(shownIndex)
            SelectionHeader(
                selectedValue = entry?.value,
                selectedLabel = entry?.label,
                average = remember(entries) { averageOf(entries.map { it.value }) },
                averageLabel = averageLabel,
                valueFormatter = valueFormatter,
                color = colors.contentColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            LineChartPlot(
                strip = strip,
                selectedIndex = shownIndex,
                onPointClick = onSelectedIndexChange,
                isAnimated = entryAnimationSpec != null,
                valueFormatter = valueFormatter,
                colors = colors,
                pointSize = pointSize,
                lineWidth = lineWidth,
                pointGap = pointGap,
                plotHeight = plotHeight
            )
            Spacer(modifier = Modifier.height(4.dp))
            StripAxis(
                strip = strip,
                selectedIndex = shownIndex,
                viewportWidth = viewportWidth,
                label = { entry, isExtended -> if (isExtended) entry.selectedAxisLabel else entry.axisLabel },
                color = colors.labelColor,
                selectedColor = colors.contentColor
            )
        }
    }
}

@Composable
private fun LineChartPlot(
    strip: EntryStrip<LineChartEntry>,
    selectedIndex: Int,
    onPointClick: (Int) -> Unit,
    isAnimated: Boolean,
    valueFormatter: (Double) -> String,
    colors: LineChartColors,
    pointSize: Dp,
    lineWidth: Dp,
    pointGap: Dp,
    plotHeight: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(plotHeight)
            .then(if (isAnimated) Modifier.clipToBounds() else Modifier)
    ) {
        // Selecting an entry, or clearing the selection when it's the selected one; entries sliding out can't be.
        val toggle: (StripItem<LineChartEntry>) -> Unit = { item ->
            if (item.index >= 0 && item.entry.value != null) onPointClick(if (item.index == selectedIndex) -1 else item.index)
        }
        val currentStrip by rememberUpdatedState(strip)
        val currentToggle by rememberUpdatedState(toggle)
        Box(
            modifier = Modifier
                .stripWindow(strip)
                .fillMaxSize()
                // A tap anywhere in a slot picks its entry, so points are easy to hit.
                .pointerInput(Unit) {
                    detectTapGestures { position ->
                        val items = currentStrip.items
                        items.getOrNull((position.x / size.width * items.size).toInt())?.let(currentToggle)
                    }
                }
        ) {
            // Fractions are read while drawing and placing, so animation frames don't recompose.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val items = strip.items
                if (items.isEmpty()) return@Canvas
                val radius = pointSize.toPx() / 2
                val centers = items.indices.map { pointCenter(it, items.size, items[it].fraction(), size, radius) }
                // Each segment stops short of its points, its round caps included.
                val inset = radius + pointGap.toPx() + lineWidth.toPx() / 2
                for (i in 1 until items.size) {
                    // Entries without data break the line.
                    if (items[i - 1].entry.value == null || items[i].entry.value == null) continue
                    val start = centers[i - 1]
                    val end = centers[i]
                    val length = (end - start).getDistance()
                    if (length <= 2 * inset) continue
                    val shift = (end - start) * (inset / length)
                    drawLine(colors.lineColor, start + shift, end - shift, lineWidth.toPx(), StrokeCap.Round)
                }
                items.forEachIndexed { i, item ->
                    if (item.entry.value != null) drawCircle(colors.pointColor, radius, centers[i])
                }
            }
            PointNodes(strip = strip, selectedIndex = selectedIndex, onToggle = toggle, valueFormatter = valueFormatter, pointSize = pointSize)
        }
    }
}

/** Where the point of the [index]-th of [count] slots sits in a plot of [size], keeping a point of [radius] inside it. */
private fun pointCenter(index: Int, count: Int, fraction: Float, size: Size, radius: Float): Offset =
    Offset((index + 0.5f) * size.width / count, radius + (1f - fraction) * (size.height - 2 * radius))

/** An invisible node over each drawn point, for accessibility services, which select it through [onToggle]. */
@Composable
private fun PointNodes(
    strip: EntryStrip<LineChartEntry>,
    selectedIndex: Int,
    onToggle: (StripItem<LineChartEntry>) -> Unit,
    valueFormatter: (Double) -> String,
    pointSize: Dp
) {
    val items = strip.items
    val withData = items.indices.filter { items[it].entry.value != null }
    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            withData.forEach { i ->
                val item = items[i]
                val description = stringResource(R.string.charts_item_description, item.entry.label, valueFormatter(item.entry.value!!))
                Box(
                    modifier = Modifier
                        .size(pointSize)
                        .semantics {
                            contentDescription = description
                            role = Role.Button
                            selected = item.index >= 0 && item.index == selectedIndex
                            if (item.index >= 0) {
                                onClick {
                                    onToggle(item)
                                    true
                                }
                            }
                        }
                )
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            val size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
            val radius = pointSize.toPx() / 2
            placeables.forEachIndexed { j, placeable ->
                val i = withData[j]
                val center = pointCenter(i, items.size, items[i].fraction(), size, radius)
                placeable.place((center.x - placeable.width / 2f).roundToInt(), (center.y - placeable.height / 2f).roundToInt())
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
        axisLabel = month,
        selectedAxisLabel = "$month/${year % 100}"
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun LineChartPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            LineChart(entries = previewEntries(12), averageLabel = "average")
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun LineChartManyEntriesPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            LineChart(entries = previewEntries(24), averageLabel = "average", entryAnimationSpec = LineChartDefaults.EntryAnimationSpec)
        }
    }
}
