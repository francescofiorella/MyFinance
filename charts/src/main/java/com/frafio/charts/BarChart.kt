package com.frafio.charts

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.frafio.charts.internal.EntryStrip
import com.frafio.charts.internal.SelectionHeader
import com.frafio.charts.internal.StripAxis
import com.frafio.charts.internal.averageOf
import com.frafio.charts.internal.maxVisibleBars
import com.frafio.charts.internal.rememberEntryStrip
import com.frafio.charts.internal.shownSelection
import com.frafio.charts.internal.stripWindow
import java.text.NumberFormat
import kotlin.math.roundToInt

/**
 * A bar of a [BarChart]. [key] orders the bars and identifies one across entry changes; keys need
 * not be consecutive (e.g. months since year zero, with gaps). [label] is shown in the header once selected
 * and read by accessibility services, [axisLabel] under the bar, and [selectedAxisLabel] under it once selected
 * or, with nothing selected, under the latest bar.
 * A null [value] means no data: the slot and its axis label stay, but no bar is drawn and it can't be selected.
 */
data class BarChartEntry(
    val key: Int,
    val value: Double?,
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

    /** Formats the header value or average, the reference label and the bars' descriptions, in the default locale. */
    val ValueFormatter: (Double) -> String = { NumberFormat.getInstance().format(it) }

    /** How the strip scrolls, zooms and rescales when the entries change; pass it as `entryAnimationSpec`. */
    val EntryAnimationSpec: FiniteAnimationSpec<Float>
        @Composable get() = MaterialTheme.motionScheme.slowSpatialSpec()

    @Composable
    fun colors(
        barColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        selectedBarColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        secondaryContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ): BarChartColors = BarChartColors(barColor, selectedBarColor, contentColor, secondaryContentColor)
}

/**
 * A bar chart that keeps its own selection, starting with none. [onBarClick] reports the selected bar,
 * or -1 once tapping it again clears the selection. With an [entryAnimationSpec], entry changes animate
 * as one strip of equal bars, matched by key.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    entries: List<BarChartEntry>,
    averageLabel: String = "",
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
        averageLabel = averageLabel,
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
 * A bar chart whose selection is hoisted. The header shows the selected entry, or with nothing selected the
 * average of the entries with data, labelled [averageLabel]. A [selectedIndex] outside [entries] (e.g. -1) or
 * on an entry without data selects nothing; tapping the selected bar clears the selection, reporting -1.
 * With an [entryAnimationSpec], entry changes animate as one strip of equal bars, matched by key: the shown
 * keys scroll and zoom to the new ones, so added bars slide in and removed ones slide out at the edges.
 */
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    entries: List<BarChartEntry>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    averageLabel: String = "",
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

        val shownIndex = shownSelection(entries.map { it.value }, selectedIndex)
        val maxValue = remember(entries) {
            val max = entries.mapNotNull { it.value }.maxOrNull() ?: 0.0
            if (max == 0.0) 1.0 else max
        }
        val strip = rememberEntryStrip(entries, maxValue, BarChartEntry::key, BarChartEntry::value, entryAnimationSpec)
        val viewportWidth = maxWidth

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
            BarChartBars(
                strip = strip,
                selectedIndex = shownIndex,
                onBarClick = onSelectedIndexChange,
                maxValue = maxValue,
                referenceValue = referenceValue,
                latestValue = entries.lastOrNull { it.value != null }?.value,
                isAnimated = entryAnimationSpec != null,
                valueFormatter = valueFormatter,
                referenceValueFormatter = referenceValueFormatter,
                colors = colors,
                barWidth = barWidth,
                barPadding = barPadding,
                barMaxHeight = barMaxHeight
            )
            Spacer(modifier = Modifier.height(4.dp))
            StripAxis(
                strip = strip,
                selectedIndex = shownIndex,
                viewportWidth = viewportWidth,
                label = { entry, isExtended -> if (isExtended) entry.selectedAxisLabel else entry.axisLabel },
                color = colors.secondaryContentColor,
                selectedColor = colors.contentColor
            )
        }
    }
}

/** What a bar shows, passed as one value so an animated bar can move between positions with its state. */
private class ChartBarState(
    val modifier: Modifier,
    val hasValue: Boolean,
    val contentDescription: String,
    val heightFraction: Float,
    val animateHeight: Boolean,
    val isSelected: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    val interactionSource: MutableInteractionSource,
    val colors: BarChartColors,
    val barWidth: Dp,
    val barPadding: Dp
)

@Composable
private fun BarChartBars(
    strip: EntryStrip<BarChartEntry>,
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
            val keys = strip.items.mapTo(HashSet()) { it.entry.key }
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
                strip.items.forEach { bar ->
                    val key = bar.entry.key
                    val value = bar.entry.value
                    val interactionSource = interactionSources.getOrPut(key) { MutableInteractionSource() }
                    customItem(
                        {
                            val state = ChartBarState(
                                modifier = Modifier
                                    .weight(1f)
                                    .animateWidth(interactionSource),
                                hasValue = value != null,
                                contentDescription = value?.let {
                                    stringResource(R.string.charts_item_description, bar.entry.label, valueFormatter(it))
                                } ?: "",
                                // A bar with data stays visible at zero.
                                heightFraction = if (value != null) bar.fraction().coerceAtLeast(0.01f) else 0f,
                                animateHeight = !isAnimated,
                                isSelected = bar.index >= 0 && selectedIndex == bar.index,
                                onCheckedChange = { checked -> if (bar.index >= 0) onBarClick(if (checked) bar.index else -1) },
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
            val heightFraction = (referenceValue / maxValue).toFloat()
            val animatedFraction by animateFloatAsState(
                targetValue = heightFraction,
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                label = "ReferenceHeight"
            )
            // Shown by its target, so a spring overshooting past the top doesn't make it flicker.
            val animatedHeightFraction = animatedFraction.coerceIn(0f, 1f)
            if (latestValue != null && heightFraction <= 1f) {
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
private fun ChartBar(state: ChartBarState) {
    // Without data the slot stays empty: nothing to draw, select or announce.
    if (!state.hasValue) {
        Spacer(modifier = state.modifier.padding(horizontal = state.barPadding).width(state.barWidth))
        return
    }
    val animatedHeightFraction by animateFloatAsState(
        targetValue = state.heightFraction,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "BarHeight"
    )
    ToggleButton(
        checked = state.isSelected,
        onCheckedChange = state.onCheckedChange,
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
            BarChart(entries = previewEntries(12), averageLabel = "average", referenceValue = 190.0)
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun BarChartManyEntriesPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(entries = previewEntries(24), averageLabel = "average", referenceValue = 190.0, entryAnimationSpec = BarChartDefaults.EntryAnimationSpec)
        }
    }
}
