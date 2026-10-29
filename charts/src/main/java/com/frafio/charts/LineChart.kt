package com.frafio.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.lerp
import androidx.graphics.shapes.RoundedPolygon
import com.frafio.charts.internal.EntryStrip
import com.frafio.charts.internal.SelectionHeader
import com.frafio.charts.internal.ShapeMorph
import com.frafio.charts.internal.StripAxis
import com.frafio.charts.internal.StripItem
import com.frafio.charts.internal.averageOf
import com.frafio.charts.internal.followPresses
import com.frafio.charts.internal.maxVisibleBars
import com.frafio.charts.internal.rememberEntryStrip
import com.frafio.charts.internal.shownSelection
import com.frafio.charts.internal.stripWindow
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

/**
 * The colors of a [LineChart]: [labelColor] for the axis labels, [contentColor] for the header and the selected
 * axis label, [guideColor] for the line from the selected point down to its label.
 */
@Immutable
data class LineChartColors(
    val lineColor: Color,
    val pointColor: Color,
    val labelColor: Color,
    val contentColor: Color,
    val guideColor: Color
)

object LineChartDefaults {
    val PointSize: Dp = 8.dp
    val SelectedPointSize: Dp = 16.dp
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val PointShape: RoundedPolygon = MaterialShapes.Circle
    /** The shape the selected point morphs into, e.g. `MaterialShapes.SoftBurst`. */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    val SelectedPointShape: RoundedPolygon = MaterialShapes.Circle
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
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        guideColor: Color = MaterialTheme.colorScheme.outlineVariant
    ): LineChartColors = LineChartColors(lineColor, pointColor, labelColor, contentColor, guideColor)
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
    selectedPointSize: Dp = LineChartDefaults.SelectedPointSize,
    pointShape: RoundedPolygon = LineChartDefaults.PointShape,
    selectedPointShape: RoundedPolygon = LineChartDefaults.SelectedPointShape,
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
        selectedPointSize = selectedPointSize,
        pointShape = pointShape,
        selectedPointShape = selectedPointShape,
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
 * selection, reporting -1. The selected point grows to [selectedPointSize] and morphs from [pointShape] into
 * [selectedPointShape], and a guide line drops from it to its axis label. With an [entryAnimationSpec], entry changes animate as one strip matched by key:
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
    selectedPointSize: Dp = LineChartDefaults.SelectedPointSize,
    pointShape: RoundedPolygon = LineChartDefaults.PointShape,
    selectedPointShape: RoundedPolygon = LineChartDefaults.SelectedPointShape,
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
                selectedPointSize = selectedPointSize,
                pointShape = pointShape,
                selectedPointShape = selectedPointShape,
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
    selectedPointSize: Dp,
    pointShape: RoundedPolygon,
    selectedPointShape: RoundedPolygon,
    lineWidth: Dp,
    pointGap: Dp,
    plotHeight: Dp
) {
    val selectionSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    // Keyed by entry, so a point keeps its state while it slides along the strip.
    val points = strip.items.associate { item ->
        key(item.entry.key) {
            val interactionSource = remember { MutableInteractionSource() }
            val pulse = remember { Animatable(0f) }
            LaunchedEffect(interactionSource, pulse, selectionSpec) {
                pulse.followPresses(interactionSource.interactions, selectionSpec)
            }
            val isSelected = item.isSelectable && item.index == selectedIndex
            // Grown while selected or while a press pulse rises: one animation, so a press that selects stays grown throughout.
            val growth = animateFloatAsState(
                targetValue = if (isSelected || pulse.targetValue == 1f) 1f else 0f,
                animationSpec = selectionSpec,
                label = "point_growth"
            )
            val selection = animateFloatAsState(
                targetValue = if (isSelected) 1f else 0f,
                animationSpec = selectionSpec,
                label = "point_selection"
            )
            item.entry.key to PointState(interactionSource, growth, selection)
        }
    }
    val growthOf: (StripItem<LineChartEntry>) -> Float = { points[it.entry.key]?.growth?.value ?: 0f }
    val selectionOf: (StripItem<LineChartEntry>) -> Float = { points[it.entry.key]?.selection?.value ?: 0f }
    val sizeOf: (StripItem<LineChartEntry>) -> Dp = { lerp(pointSize, selectedPointSize, growthOf(it)) }
    // Points keep clear of the plot's edges at their largest.
    val insetRadius = maxOf(pointSize, selectedPointSize) / 2
    val morph = remember(pointShape, selectedPointShape) { ShapeMorph(pointShape, selectedPointShape) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(plotHeight)
            .then(if (isAnimated) Modifier.clipToBounds() else Modifier)
    ) {
        // Selecting an entry, or clearing the selection when it's the selected one.
        val toggle: (StripItem<LineChartEntry>) -> Unit = { item ->
            if (item.isSelectable) onPointClick(if (item.index == selectedIndex) -1 else item.index)
        }
        val currentStrip by rememberUpdatedState(strip)
        val currentToggle by rememberUpdatedState(toggle)
        val currentPoints by rememberUpdatedState(points)
        Box(
            modifier = Modifier
                .stripWindow(strip)
                .fillMaxSize()
                // A touch anywhere in a slot picks its entry, so points are easy to hit.
                .pointerInput(Unit) {
                    fun itemAt(position: Offset): StripItem<LineChartEntry>? =
                        currentStrip.items.let { it.getOrNull((position.x / size.width * it.size).toInt()) }
                    detectTapGestures(
                        onPress = press@{ position ->
                            val item = itemAt(position)?.takeIf { it.isSelectable } ?: return@press
                            val source = currentPoints[item.entry.key]?.interactionSource ?: return@press
                            val press = PressInteraction.Press(position)
                            source.emit(press)
                            source.emit(if (tryAwaitRelease()) PressInteraction.Release(press) else PressInteraction.Cancel(press))
                        },
                        onTap = { position -> itemAt(position)?.let(currentToggle) }
                    )
                }
        ) {
            // Fractions and progresses are read while drawing and placing, so animation frames don't recompose.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val items = strip.items
                if (items.isEmpty()) return@Canvas
                val centers = items.indices.map { pointCenter(it, items.size, items[it].fraction(), size, insetRadius.toPx()) }
                val radii = items.map { sizeOf(it).toPx() / 2 }
                // The selected point's guide, fading in and out with the selection but not with presses, keeps the same gap below it as the line.
                items.forEachIndexed { i, item ->
                    val progress = selectionOf(item).coerceIn(0f, 1f)
                    if (item.entry.value == null || progress == 0f) return@forEachIndexed
                    val top = centers[i].y + radii[i] + pointGap.toPx()
                    if (top >= size.height) return@forEachIndexed
                    drawLine(
                        color = colors.guideColor,
                        start = Offset(centers[i].x, top),
                        end = Offset(centers[i].x, size.height),
                        strokeWidth = GuideWidth.toPx(),
                        alpha = progress
                    )
                }
                // Each segment stops short of its two points, as they are now, its round caps included.
                val clearance = pointGap.toPx() + lineWidth.toPx() / 2
                for (i in 1 until items.size) {
                    // Entries without data break the line.
                    if (items[i - 1].entry.value == null || items[i].entry.value == null) continue
                    val start = centers[i - 1]
                    val end = centers[i]
                    val length = (end - start).getDistance()
                    val startInset = radii[i - 1] + clearance
                    val endInset = radii[i] + clearance
                    if (length <= startInset + endInset) continue
                    val direction = (end - start) / length
                    drawLine(colors.lineColor, start + direction * startInset, end - direction * endInset, lineWidth.toPx(), StrokeCap.Round)
                }
                items.forEachIndexed { i, item ->
                    if (item.entry.value == null) return@forEachIndexed
                    morph.draw(this, growthOf(item), Size(radii[i] * 2, radii[i] * 2), centers[i], colors.pointColor)
                }
            }
            PointNodes(
                strip = strip,
                selectedIndex = selectedIndex,
                onToggle = toggle,
                valueFormatter = valueFormatter,
                sizeOf = sizeOf,
                insetRadius = insetRadius
            )
        }
    }
}

private val GuideWidth: Dp = 1.dp

/** A point's press source and progresses: [growth] while selected or pressed, [selection] while selected. */
private class PointState(
    val interactionSource: MutableInteractionSource,
    val growth: State<Float>,
    val selection: State<Float>
)

/** Entries sliding out (index -1) and entries without data can't be selected. */
private val StripItem<LineChartEntry>.isSelectable: Boolean
    get() = index >= 0 && entry.value != null

/** Where the point of the [index]-th of [count] slots sits in a plot of [size], keeping a point of [radius] inside it. */
private fun pointCenter(index: Int, count: Int, fraction: Float, size: Size, radius: Float): Offset =
    Offset((index + 0.5f) * size.width / count, radius + (1f - fraction) * (size.height - 2 * radius))

/**
 * An invisible node over each drawn point, sized as it is drawn ([sizeOf]), for accessibility services,
 * which select it through [onToggle].
 */
@Composable
private fun PointNodes(
    strip: EntryStrip<LineChartEntry>,
    selectedIndex: Int,
    onToggle: (StripItem<LineChartEntry>) -> Unit,
    valueFormatter: (Double) -> String,
    sizeOf: (StripItem<LineChartEntry>) -> Dp,
    insetRadius: Dp
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
                    modifier = Modifier.semantics {
                        contentDescription = description
                        role = Role.Button
                        selected = item.isSelectable && item.index == selectedIndex
                        if (item.isSelectable) {
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
        val placeables = measurables.mapIndexed { j, measurable ->
            val size = sizeOf(items[withData[j]]).roundToPx().coerceAtLeast(0)
            measurable.measure(Constraints.fixed(size, size))
        }
        layout(constraints.maxWidth, constraints.maxHeight) {
            val size = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
            placeables.forEachIndexed { j, placeable ->
                val i = withData[j]
                val center = pointCenter(i, items.size, items[i].fraction(), size, insetRadius.toPx())
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
