package com.frafio.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * A bar of a [BarChart]. [key] orders the bars and identifies one across entry changes; keys need
 * not be consecutive (e.g. months since year zero, with gaps). [label] is shown in the header and
 * read by accessibility services, [axisLabel] under the bar, and [selectedAxisLabel] under it once selected.
 */
data class BarChartEntry(
    val key: Int,
    val value: Double,
    val label: String,
    val axisLabel: String,
    val selectedAxisLabel: String = axisLabel
)

/**
 * The colors of a [BarChart]: [contentColor] for the header and the selected axis label,
 * [secondaryContentColor] for the other axis labels and the reference line.
 */
@Immutable
data class BarChartColors(
    val barColor: Color,
    val selectedBarColor: Color,
    val contentColor: Color,
    val secondaryContentColor: Color
)

object BarChartDefaults {
    val BarWidth: Dp = 40.dp
    val BarPadding: Dp = 3.dp
    val BarMaxHeight: Dp = 160.dp

    /** Formats the header value, the reference label and the bars' descriptions, in the default locale. */
    val ValueFormatter: (Double) -> String = { NumberFormat.getInstance().format(it) }

    @Composable
    fun colors(
        barColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        selectedBarColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        secondaryContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ): BarChartColors = BarChartColors(barColor, selectedBarColor, contentColor, secondaryContentColor)
}

/** How many bars of [barWidth] plus [barPadding] on each side fit in [maxWidth]; at least one. */
internal fun maxVisibleBars(maxWidth: Dp, barWidth: Dp, barPadding: Dp): Int =
    (maxWidth / (barWidth + barPadding * 2)).toInt().coerceAtLeast(1)

/** Narrowest bar slot that still fits a two-digit axis label. */
internal val MinAxisLabelSlot: Dp = 20.dp

/** Every how many bars the axis shows a label, so labels on slots of [slot] width don't overlap. */
internal fun axisLabelStep(slot: Dp): Int =
    if (slot.value <= 0f || slot >= MinAxisLabelSlot) 1 else ceil(MinAxisLabelSlot / slot).toInt()

/**
 * Whether the bar at [position] gets an axis label: every [step]-th bar counting back from
 * [latestPosition], plus the selected one, whose neighbours closer than [step] stay unlabelled.
 */
internal fun isAxisLabelShown(position: Int, latestPosition: Int, selectedPosition: Int, step: Int): Boolean =
    position == selectedPosition ||
        (Math.floorMod(latestPosition - position, step) == 0 && abs(position - selectedPosition) >= step)

/**
 * Where [key] falls among the sorted bar [keys], as a fractional bar index: between two bars it is
 * proportional to the keys in between, outside them one bar per key.
 */
internal fun stripIndexAt(keys: List<Int>, key: Float): Float {
    if (keys.isEmpty()) return 0f
    if (key <= keys.first()) return key - keys.first()
    if (key >= keys.last()) return keys.lastIndex + (key - keys.last())
    val i = keys.indexOfLast { it <= key }
    return i + (key - keys[i]) / (keys[i + 1] - keys[i])
}

/**
 * A bar chart that keeps its own selection, starting on the latest entry. With an
 * [entryAnimationSpec], entry changes animate as one strip of equal bars, matched by key.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    entries: List<BarChartEntry>,
    referenceValue: Double? = null,
    onBarClick: (Int) -> Unit = {},
    onVisibleCountChanged: (Int) -> Unit = {},
    valueFormatter: (Double) -> String = BarChartDefaults.ValueFormatter,
    referenceValueFormatter: (Double) -> String = valueFormatter,
    colors: BarChartColors = BarChartDefaults.colors(),
    barWidth: Dp = BarChartDefaults.BarWidth,
    barPadding: Dp = BarChartDefaults.BarPadding,
    barMaxHeight: Dp = BarChartDefaults.BarMaxHeight,
    entryAnimationSpec: FiniteAnimationSpec<Float>? = null
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    BarChart(
        modifier = modifier,
        entries = entries,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = {
            selectedIndex = it
            onBarClick(it)
        },
        referenceValue = referenceValue,
        onVisibleCountChanged = onVisibleCountChanged,
        valueFormatter = valueFormatter,
        referenceValueFormatter = referenceValueFormatter,
        colors = colors,
        barWidth = barWidth,
        barPadding = barPadding,
        barMaxHeight = barMaxHeight,
        entryAnimationSpec = entryAnimationSpec
    )
}

/**
 * A bar chart whose selection is hoisted. A [selectedIndex] outside [entries] (e.g. -1) selects the
 * latest entry, so resetting it to -1 goes back to the default. With an [entryAnimationSpec], entry
 * changes animate as one strip of equal bars, matched by key: the shown keys scroll and zoom to
 * the new ones, so added bars slide in and removed ones slide out at the edges.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    entries: List<BarChartEntry>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    referenceValue: Double? = null,
    onVisibleCountChanged: (Int) -> Unit = {},
    valueFormatter: (Double) -> String = BarChartDefaults.ValueFormatter,
    referenceValueFormatter: (Double) -> String = valueFormatter,
    colors: BarChartColors = BarChartDefaults.colors(),
    barWidth: Dp = BarChartDefaults.BarWidth,
    barPadding: Dp = BarChartDefaults.BarPadding,
    barMaxHeight: Dp = BarChartDefaults.BarMaxHeight,
    entryAnimationSpec: FiniteAnimationSpec<Float>? = null
) {
    BoxWithConstraints(modifier = modifier) {
        val maxVisibleItems = maxVisibleBars(maxWidth, barWidth, barPadding)

        LaunchedEffect(maxVisibleItems) {
            onVisibleCountChanged(maxVisibleItems)
        }

        val shownIndex = if (selectedIndex in entries.indices) selectedIndex else entries.lastIndex
        val maxValue = remember(entries) {
            val max = entries.maxOfOrNull { it.value } ?: 0.0
            if (max == 0.0) 1.0 else max
        }
        val strip = rememberBarStrip(entries, maxValue, entryAnimationSpec)
        val viewportWidth = maxWidth

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            BarChartHeader(entry = entries.getOrNull(shownIndex), valueFormatter = valueFormatter, colors = colors)
            Spacer(modifier = Modifier.height(16.dp))
            BarChartBars(
                strip = strip,
                selectedIndex = shownIndex,
                onBarClick = onSelectedIndexChange,
                maxValue = maxValue,
                referenceValue = referenceValue,
                latestValue = entries.lastOrNull()?.value,
                isAnimated = entryAnimationSpec != null,
                valueFormatter = valueFormatter,
                referenceValueFormatter = referenceValueFormatter,
                colors = colors,
                barWidth = barWidth,
                barPadding = barPadding,
                barMaxHeight = barMaxHeight
            )
            Spacer(modifier = Modifier.height(4.dp))
            BarChartAxis(strip = strip, selectedIndex = shownIndex, viewportWidth = viewportWidth, colors = colors)
        }
    }
}

private fun BarChartEntry.heightFraction(maxValue: Double): Float =
    (value / maxValue).toFloat().coerceIn(0.01f, 1f)

/** A bar as drawn: [index] points into the entries, or is -1 while the bar slides out. */
private class DrawnBar(
    val entry: BarChartEntry,
    val index: Int,
    val heightFraction: () -> Float
)

/**
 * Equal bars side by side, of which the ones from [start] to [end] (fractional bar indices) fill the
 * chart width; the others lie outside it.
 */
private class BarStrip(
    val bars: List<DrawnBar>,
    val start: () -> Float,
    val end: () -> Float
)

/** Lays the content out as the whole [strip], scaled and shifted so its visible window fills the width. */
private fun Modifier.stripWindow(strip: BarStrip): Modifier = layout { measurable, constraints ->
    val width = constraints.maxWidth
    val start = strip.start()
    val slot = width / (strip.end() - start + 1f).coerceAtLeast(1f)
    val stripWidth = (slot * strip.bars.size).roundToInt()
    val placeable = measurable.measure(constraints.copy(minWidth = stripWidth, maxWidth = stripWidth))
    layout(width, placeable.height) {
        placeable.place((-start * slot).roundToInt(), 0)
    }
}

/**
 * Animation state keyed by entry key rather than position, since bars are added and removed on
 * either side and the [ButtonGroup] composes its items by position.
 */
private class BarTransitions {
    private val heights = HashMap<Int, Animatable<Float, AnimationVector1D>>()
    // Keys of the first and last bar filling the width.
    private var first: Animatable<Float, AnimationVector1D>? = null
    private var last: Animatable<Float, AnimationVector1D>? = null
    private var drawn: List<DrawnBar> = emptyList()

    /** The strip for [entries], keeping the removed bars until they have slid out. */
    fun update(entries: List<BarChartEntry>, maxValue: Double): BarStrip {
        if (entries.isEmpty()) {
            heights.clear()
            first = null
            last = null
            drawn = emptyList()
            return BarStrip(emptyList(), { 0f }, { 0f })
        }
        // The first entries are shown at once, without sliding in.
        val first = first ?: Animatable(entries.first().key.toFloat()).also { first = it }
        val last = last ?: Animatable(entries.last().key.toFloat()).also { last = it }
        val current = entries.mapIndexed { index, entry ->
            val h = heights.getOrPut(entry.key) { Animatable(entry.heightFraction(maxValue)) }
            DrawnBar(entry, index) { h.value }
        }
        val currentKeys = entries.mapTo(HashSet()) { it.key }
        val leaving = drawn
            .filter { it.entry.key !in currentKeys && it.entry.key in heights }
            .map { DrawnBar(it.entry, -1, it.heightFraction) }
        drawn = (current + leaving).sortedBy { it.entry.key }
        val keys = drawn.map { it.entry.key }
        return BarStrip(drawn, { stripIndexAt(keys, first.value) }, { stripIndexAt(keys, last.value) })
    }

    /** Moves the window onto [entries] and rescales every drawn bar, then forgets the removed ones. */
    suspend fun animateTo(entries: List<BarChartEntry>, maxValue: Double, spec: FiniteAnimationSpec<Float>) {
        val first = first ?: return
        val last = last ?: return
        val bars = drawn
        coroutineScope {
            launch { first.animateTo(entries.first().key.toFloat(), spec) }
            launch { last.animateTo(entries.last().key.toFloat(), spec) }
            bars.forEach { bar ->
                val h = heights[bar.entry.key] ?: return@forEach
                launch { h.animateTo(bar.entry.heightFraction(maxValue), spec) }
            }
        }
        heights.keys.retainAll(entries.mapTo(HashSet()) { it.key })
    }
}

@Composable
private fun rememberBarStrip(
    entries: List<BarChartEntry>,
    maxValue: Double,
    animationSpec: FiniteAnimationSpec<Float>?
): BarStrip {
    if (animationSpec == null) {
        return remember(entries, maxValue) {
            val bars = entries.mapIndexed { index, entry ->
                val fraction = entry.heightFraction(maxValue)
                DrawnBar(entry, index) { fraction }
            }
            BarStrip(bars, { 0f }, { (bars.size - 1).toFloat() })
        }
    }
    val transitions = remember { BarTransitions() }
    // Bumped once removed bars have slid out, so they are dropped from the strip.
    var settled by remember { mutableIntStateOf(0) }
    val strip = remember(entries, maxValue, settled) { transitions.update(entries, maxValue) }
    LaunchedEffect(entries, maxValue) {
        transitions.animateTo(entries, maxValue, animationSpec)
        settled++
    }
    return strip
}

@Composable
private fun BarChartHeader(entry: BarChartEntry?, valueFormatter: (Double) -> String, colors: BarChartColors) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = entry?.let { valueFormatter(it.value) } ?: "",
            color = colors.contentColor,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = entry?.label ?: "",
            color = colors.contentColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/** What a bar shows, passed as one value so an animated bar can move between positions with its state. */
private class ChartBarState(
    val modifier: Modifier,
    val contentDescription: String,
    val heightFraction: Float,
    val animateHeight: Boolean,
    val isSelected: Boolean,
    val onClick: () -> Unit,
    val interactionSource: MutableInteractionSource,
    val colors: BarChartColors,
    val barWidth: Dp,
    val barPadding: Dp
)

@Composable
private fun BarChartBars(
    strip: BarStrip,
    selectedIndex: Int,
    onBarClick: (Int) -> Unit,
    maxValue: Double,
    referenceValue: Double?,
    latestValue: Double?,
    isAnimated: Boolean,
    valueFormatter: (Double) -> String,
    referenceValueFormatter: (Double) -> String,
    colors: BarChartColors,
    barWidth: Dp,
    barPadding: Dp,
    barMaxHeight: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barMaxHeight),
        contentAlignment = Alignment.BottomStart
    ) {
        val interactionSources = remember { HashMap<Int, MutableInteractionSource>() }
        // The ButtonGroup composes by position, while animated bars shift position as others slide
        // in and out: movable content keeps each bar's state with its key.
        val movableBars = remember { HashMap<Int, @Composable (ChartBarState) -> Unit>() }
        SideEffect {
            val keys = strip.bars.mapTo(HashSet()) { it.entry.key }
            movableBars.keys.retainAll(keys)
            interactionSources.keys.retainAll(keys)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (isAnimated) Modifier.clipToBounds() else Modifier),
            contentAlignment = Alignment.BottomStart
        ) {
            ButtonGroup(
                modifier = Modifier
                    .stripWindow(strip)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
                overflowIndicator = { menuState ->
                    ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                }
            ) {
                strip.bars.forEach { bar ->
                    val key = bar.entry.key
                    val interactionSource = interactionSources.getOrPut(key) { MutableInteractionSource() }
                    customItem(
                        {
                            val state = ChartBarState(
                                modifier = Modifier
                                    .weight(1f)
                                    .animateWidth(interactionSource),
                                contentDescription = stringResource(
                                    R.string.charts_item_description,
                                    bar.entry.label,
                                    valueFormatter(bar.entry.value)
                                ),
                                heightFraction = bar.heightFraction(),
                                animateHeight = !isAnimated,
                                isSelected = bar.index >= 0 && selectedIndex == bar.index,
                                onClick = { if (bar.index >= 0) onBarClick(bar.index) },
                                interactionSource = interactionSource,
                                colors = colors,
                                barWidth = barWidth,
                                barPadding = barPadding
                            )
                            if (isAnimated) {
                                movableBars.getOrPut(key) { movableContentOf { s: ChartBarState -> ChartBar(s) } }(state)
                            } else {
                                ChartBar(state)
                            }
                        },
                        {}
                    )
                }
            }
        }

        if (referenceValue != null && referenceValue > 0) {
            val animatedHeightFraction by animateFloatAsState(
                targetValue = (referenceValue / maxValue).toFloat(),
                animationSpec = tween(durationMillis = 500),
                label = "ReferenceHeight"
            )
            if (latestValue != null && animatedHeightFraction in 0f..1f) {
                HorizontalDivider(
                    modifier = Modifier
                        .zIndex(-1f)
                        .fillMaxWidth()
                        .padding(bottom = barMaxHeight * animatedHeightFraction),
                    color = colors.secondaryContentColor
                )
                if (latestValue <= referenceValue) {
                    Text(
                        modifier = Modifier
                            .zIndex(-1f)
                            .fillMaxWidth()
                            .offset {
                                IntOffset(
                                    0,
                                    -(barMaxHeight * animatedHeightFraction).toPx()
                                        .roundToInt()
                                )
                            },
                        text = referenceValueFormatter(referenceValue),
                        color = colors.secondaryContentColor,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChartAxis(strip: BarStrip, selectedIndex: Int, viewportWidth: Dp, colors: BarChartColors) {
    val bars = strip.bars
    val latestPosition = bars.indexOfLast { it.index >= 0 }
    val selectedPosition = bars.indexOfFirst { it.index >= 0 && it.index == selectedIndex }
    // Read while composing, so labels thin out progressively as the bars narrow.
    val start = strip.start()
    val visibleBars = (strip.end() - start + 1f).coerceAtLeast(1f)
    val labelStep = axisLabelStep(viewportWidth / visibleBars)
    Row(
        modifier = Modifier
            .stripWindow(strip)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bars.forEachIndexed { position, bar ->
            if (!isAxisLabelShown(position, latestPosition, selectedPosition, labelStep)) {
                Spacer(modifier = Modifier.weight(1f))
                return@forEachIndexed
            }
            // Labels of bars outside the width are hidden rather than clipped, so edge labels can overflow.
            val center = position - start + 0.5f
            val isInside = center in 0f..visibleBars
            val isSelected = position == selectedPosition
            Text(
                text = if (isSelected) bar.entry.selectedAxisLabel else bar.entry.axisLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.contentColor else colors.secondaryContentColor,
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (isInside) 1f else 0f)
                    .wrapContentWidth(
                        align = Alignment.CenterHorizontally,
                        unbounded = true
                    ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible
            )
        }
    }
}

@Composable
private fun ChartBar(state: ChartBarState) {
    val animatedHeightFraction by animateFloatAsState(
        targetValue = state.heightFraction,
        animationSpec = tween(durationMillis = 500),
        label = "BarHeight"
    )
    ToggleButton(
        checked = state.isSelected,
        onCheckedChange = {
            if (it) state.onClick()
        },
        content = {},
        colors = ToggleButtonDefaults.colors(
            containerColor = state.colors.barColor,
            checkedContainerColor = state.colors.selectedBarColor
        ),
        modifier = state.modifier
            .semantics { this.contentDescription = state.contentDescription }
            // Animated strips drive heights themselves, keyed by entry key.
            .fillMaxHeight(if (state.animateHeight) animatedHeightFraction else state.heightFraction)
            .padding(horizontal = state.barPadding)
            .width(state.barWidth),
        interactionSource = state.interactionSource
    )
}

private fun previewEntries(count: Int): List<BarChartEntry> = (0 until count).map {
    val month = "%02d".format(it % 12 + 1)
    val year = 2023 + it / 12
    BarChartEntry(
        key = it,
        value = 60.0 + (it * 37) % 190,
        label = "$month/$year",
        axisLabel = month,
        selectedAxisLabel = "$month/${year % 100}"
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun BarChartPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(entries = previewEntries(12), referenceValue = 190.0)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun BarChartManyEntriesPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(entries = previewEntries(24), referenceValue = 190.0, entryAnimationSpec = tween(durationMillis = 500))
        }
    }
}
