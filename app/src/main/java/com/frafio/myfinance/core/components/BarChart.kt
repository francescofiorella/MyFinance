package com.frafio.myfinance.core.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

object BarChartDefaults {
    val BarWidth: Dp = 40.dp
    val BarPadding: Dp = 3.dp
    val BarMaxHeight: Dp = 160.dp
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
 * A bar chart that keeps its own selection, starting on the latest month. With an
 * [entryAnimationSpec], bars added or removed between entry changes grow in and shrink out.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    entries: List<BarChartEntry>,
    referenceValue: Double? = null,
    onBarClick: (Int) -> Unit = {},
    onVisibleCountChanged: (Int) -> Unit = {},
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
        barWidth = barWidth,
        barPadding = barPadding,
        barMaxHeight = barMaxHeight,
        entryAnimationSpec = entryAnimationSpec
    )
}

/**
 * A bar chart whose selection is hoisted. A [selectedIndex] outside [entries] (e.g. -1) selects the
 * latest month, so resetting it to -1 goes back to the default. With an [entryAnimationSpec], bars
 * added or removed between entry changes grow in and shrink out, matched by month.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    entries: List<BarChartEntry>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    referenceValue: Double? = null,
    onVisibleCountChanged: (Int) -> Unit = {},
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
        val bars = rememberDrawnBars(entries, maxValue, entryAnimationSpec)
        val minWeight = minBarWeight(bars.size, constraints.maxWidth)
        val labelStep = axisLabelStep(if (entries.isEmpty()) maxWidth else maxWidth / entries.size)

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            BarChartHeader(entry = entries.getOrNull(shownIndex))
            Spacer(modifier = Modifier.height(16.dp))
            BarChartBars(
                bars = bars,
                selectedIndex = shownIndex,
                onBarClick = onSelectedIndexChange,
                maxValue = maxValue,
                referenceValue = referenceValue,
                latestValue = entries.lastOrNull()?.value,
                animateHeights = entryAnimationSpec == null,
                minWeight = minWeight,
                barWidth = barWidth,
                barPadding = barPadding,
                barMaxHeight = barMaxHeight
            )
            Spacer(modifier = Modifier.height(4.dp))
            BarChartAxis(bars = bars, selectedIndex = shownIndex, labelStep = labelStep, minWeight = minWeight)
        }
    }
}

/**
 * The weight of a fully shrunk bar: enough for about 2 px, as the ButtonGroup loses its rounding
 * remainder on zero-width items, overflows and fails to measure.
 */
private fun minBarWeight(barCount: Int, maxWidthPx: Int): Float =
    if (maxWidthPx in 1 until Constraints.Infinity) 2f * barCount / maxWidthPx else 0.001f

/** Bars fade only while nearly shrunk, so the minimum weight leaves no visible sliver. */
private fun barAlpha(presence: Float): Float = (presence * 5f).coerceAtMost(1f)

private fun BarChartEntry.monthYear(): String =
    YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy"))

/** Months since year zero: identifies a bar across entry changes and sorts it chronologically. */
private val BarChartEntry.monthKey: Int
    get() = year * 12 + month - 1

private fun BarChartEntry.heightFraction(maxValue: Double): Float =
    (value / maxValue).toFloat().coerceIn(0.01f, 1f)

/** A bar as drawn: [index] points into the entries, or is -1 while the bar animates out. */
private class DrawnBar(
    val entry: BarChartEntry,
    val index: Int,
    val presence: () -> Float,
    val heightFraction: () -> Float
)

/**
 * Animation state keyed by month rather than position, since bars are added and removed on either
 * side and the [ButtonGroup] composes its items by position.
 */
private class BarTransitions {
    private val presence = HashMap<Int, Animatable<Float, AnimationVector1D>>()
    private val heights = HashMap<Int, Animatable<Float, AnimationVector1D>>()
    private var drawn: List<DrawnBar> = emptyList()

    /** The bars for [entries], plus the removed ones that haven't finished shrinking out. */
    fun update(entries: List<BarChartEntry>, maxValue: Double): List<DrawnBar> {
        // Bars shown first appear at once; later ones grow in.
        val initialPresence = if (drawn.isEmpty()) 1f else 0f
        val current = entries.mapIndexed { index, entry ->
            val p = presence.getOrPut(entry.monthKey) { Animatable(initialPresence) }
            val h = heights.getOrPut(entry.monthKey) { Animatable(entry.heightFraction(maxValue)) }
            DrawnBar(entry, index, { p.value }, { h.value })
        }
        val currentKeys = entries.mapTo(HashSet()) { it.monthKey }
        val leaving = drawn
            .filter { it.entry.monthKey !in currentKeys && it.entry.monthKey in presence }
            .map { DrawnBar(it.entry, -1, it.presence, it.heightFraction) }
        drawn = (current + leaving).sortedBy { it.entry.monthKey }
        return drawn
    }

    /** Grows [entries] in, shrinks the others out and rescales heights, then forgets the removed bars. */
    suspend fun animateTo(entries: List<BarChartEntry>, maxValue: Double, spec: FiniteAnimationSpec<Float>) {
        val targets = entries.associate { it.monthKey to it.heightFraction(maxValue) }
        coroutineScope {
            presence.toList().forEach { (key, p) ->
                launch { p.animateTo(if (key in targets) 1f else 0f, spec) }
            }
            targets.forEach { (key, target) ->
                heights[key]?.let { h -> launch { h.animateTo(target, spec) } }
            }
        }
        presence.keys.retainAll(targets.keys)
        heights.keys.retainAll(targets.keys)
    }
}

@Composable
private fun rememberDrawnBars(
    entries: List<BarChartEntry>,
    maxValue: Double,
    animationSpec: FiniteAnimationSpec<Float>?
): List<DrawnBar> {
    if (animationSpec == null) {
        return remember(entries, maxValue) {
            entries.mapIndexed { index, entry ->
                val fraction = entry.heightFraction(maxValue)
                DrawnBar(entry, index, { 1f }, { fraction })
            }
        }
    }
    val transitions = remember { BarTransitions() }
    // Bumped once removed bars are gone, so they are dropped from the drawn list.
    var settled by remember { mutableIntStateOf(0) }
    val bars = remember(entries, maxValue, settled) { transitions.update(entries, maxValue) }
    LaunchedEffect(entries, maxValue) {
        transitions.animateTo(entries, maxValue, animationSpec)
        settled++
    }
    return bars
}

@Composable
private fun BarChartHeader(entry: BarChartEntry?) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = entry?.let { doubleToPrice(it.value) } ?: "",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = entry?.monthYear() ?: "",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun BarChartBars(
    bars: List<DrawnBar>,
    selectedIndex: Int,
    onBarClick: (Int) -> Unit,
    maxValue: Double,
    referenceValue: Double?,
    latestValue: Double?,
    animateHeights: Boolean,
    minWeight: Float,
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
        ButtonGroup(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
            overflowIndicator = { menuState ->
                ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
            }
        ) {
            bars.forEach { bar ->
                val interactionSource = interactionSources.getOrPut(bar.entry.monthKey) { MutableInteractionSource() }
                customItem(
                    {
                        val presence = bar.presence()
                        ChartBar(
                            modifier = Modifier
                                .weight(presence.coerceAtLeast(minWeight))
                                .animateWidth(interactionSource)
                                .alpha(barAlpha(presence)),
                            barWidth = barWidth,
                            barPadding = barPadding * presence,
                            heightFraction = bar.heightFraction(),
                            animateHeight = animateHeights,
                            isSelected = bar.index >= 0 && selectedIndex == bar.index,
                            contentDescription = stringResource(
                                R.string.chart_bar,
                                bar.entry.monthYear(),
                                doubleToPrice(bar.entry.value)
                            ),
                            onClick = { if (bar.index >= 0) onBarClick(bar.index) },
                            interactionSource = interactionSource
                        )
                    },
                    {}
                )
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = doubleToPriceWithoutDecimals(referenceValue),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChartAxis(bars: List<DrawnBar>, selectedIndex: Int, labelStep: Int, minWeight: Float) {
    val latestPosition = bars.indexOfLast { it.index >= 0 }
    val selectedPosition = bars.indexOfFirst { it.index >= 0 && it.index == selectedIndex }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        bars.forEachIndexed { position, bar ->
            val presence = bar.presence()
            val weightModifier = Modifier.weight(presence.coerceAtLeast(minWeight))
            if (!isAxisLabelShown(position, latestPosition, selectedPosition, labelStep)) {
                Spacer(modifier = weightModifier)
                return@forEachIndexed
            }
            val isSelected = position == selectedPosition
            val shortLabel = "%02d".format(bar.entry.month).takeLast(2)
            val extendedLabel = shortLabel + "/" + "%02d".format(bar.entry.year).takeLast(2)
            Text(
                text = if (isSelected) extendedLabel else shortLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = weightModifier
                    .alpha(barAlpha(presence))
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
private fun ChartBar(
    modifier: Modifier = Modifier,
    barWidth: Dp = 40.dp,
    barPadding: Dp = 3.dp,
    heightFraction: Float,
    animateHeight: Boolean,
    isSelected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource
) {
    val animatedHeightFraction by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = if (animateHeight) tween(durationMillis = 500) else snap(),
        label = "BarHeight"
    )
    ToggleButton(
        checked = isSelected,
        onCheckedChange = {
            if (it) onClick()
        },
        content = {},
        colors = ToggleButtonDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            checkedContainerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .fillMaxHeight(animatedHeightFraction)
            .padding(horizontal = barPadding)
            .width(barWidth),
        interactionSource = interactionSource
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun BarChartPreview() {
    val entries = listOf(
        BarChartEntry(100.0, 2024, 1),
        BarChartEntry(150.0, 2024, 2),
        BarChartEntry(80.0, 2024, 3),
        BarChartEntry(200.0, 2024, 4),
        BarChartEntry(120.0, 2024, 5),
        BarChartEntry(90.0, 2024, 6),
        BarChartEntry(180.0, 2024, 7),
        BarChartEntry(250.0, 2024, 8),
        BarChartEntry(60.0, 2024, 9),
        BarChartEntry(140.0, 2024, 10),
        BarChartEntry(110.0, 2024, 11),
        BarChartEntry(170.0, 2024, 12)
    )

    MyFinanceTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(entries = entries, referenceValue = 190.0)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun BarChartManyEntriesPreview() {
    val entries = (0 until 24).map { BarChartEntry(60.0 + (it * 37) % 190, 2023 + it / 12, it % 12 + 1) }

    MyFinanceTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(entries = entries, referenceValue = 190.0, entryAnimationSpec = tween(durationMillis = 500))
        }
    }
}
