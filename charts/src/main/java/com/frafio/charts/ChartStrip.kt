package com.frafio.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

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
 * A negative [selectedPosition] means no selection.
 */
internal fun isAxisLabelShown(position: Int, latestPosition: Int, selectedPosition: Int, step: Int): Boolean =
    position == selectedPosition ||
        (Math.floorMod(latestPosition - position, step) == 0 && (selectedPosition < 0 || abs(position - selectedPosition) >= step))

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

/** The fraction of [maxValue] reached by [value], 0 without data. */
private fun valueFraction(value: Double?, maxValue: Double): Float =
    value?.let { (it / maxValue).toFloat().coerceIn(0f, 1f) } ?: 0f

/** An entry as drawn: [index] points into the entries, or is -1 while it slides out. */
internal class StripItem<E>(
    val entry: E,
    val index: Int,
    val fraction: () -> Float
)

/**
 * Equal slots side by side, of which the ones from [start] to [end] (fractional slot indices) fill
 * the chart width; the others lie outside it.
 */
internal class EntryStrip<E>(
    val items: List<StripItem<E>>,
    val start: () -> Float,
    val end: () -> Float
)

/** Lays the content out as the whole [strip], scaled and shifted so its visible window fills the width. */
internal fun Modifier.stripWindow(strip: EntryStrip<*>): Modifier = layout { measurable, constraints ->
    val width = constraints.maxWidth
    val start = strip.start()
    val slot = width / (strip.end() - start + 1f).coerceAtLeast(1f)
    val stripWidth = (slot * strip.items.size).roundToInt()
    val placeable = measurable.measure(constraints.copy(minWidth = stripWidth, maxWidth = stripWidth))
    layout(width, placeable.height) {
        placeable.place((-start * slot).roundToInt(), 0)
    }
}

/** Animation state keyed by entry [key] rather than position, since entries are added and removed on either side. */
private class StripTransitions<E>(
    private val key: (E) -> Int,
    private val value: (E) -> Double?
) {
    private val fractions = HashMap<Int, Animatable<Float, AnimationVector1D>>()
    // Keys of the first and last entry filling the width.
    private var first: Animatable<Float, AnimationVector1D>? = null
    private var last: Animatable<Float, AnimationVector1D>? = null
    private var drawn: List<StripItem<E>> = emptyList()

    /** The strip for [entries], keeping the removed ones until they have slid out. */
    fun update(entries: List<E>, maxValue: Double): EntryStrip<E> {
        if (entries.isEmpty()) {
            fractions.clear()
            first = null
            last = null
            drawn = emptyList()
            return EntryStrip(emptyList(), { 0f }, { 0f })
        }
        // The first entries are shown at once, without sliding in.
        val first = first ?: Animatable(key(entries.first()).toFloat()).also { first = it }
        val last = last ?: Animatable(key(entries.last()).toFloat()).also { last = it }
        val current = entries.mapIndexed { index, entry ->
            val f = fractions.getOrPut(key(entry)) { Animatable(valueFraction(value(entry), maxValue)) }
            StripItem(entry, index) { f.value }
        }
        val currentKeys = entries.mapTo(HashSet(), key)
        val leaving = drawn
            .filter { key(it.entry) !in currentKeys && key(it.entry) in fractions }
            .map { StripItem(it.entry, -1, it.fraction) }
        drawn = (current + leaving).sortedBy { key(it.entry) }
        val keys = drawn.map { key(it.entry) }
        return EntryStrip(drawn, { stripIndexAt(keys, first.value) }, { stripIndexAt(keys, last.value) })
    }

    /** Moves the window onto [entries] and rescales every drawn item, then forgets the removed ones. */
    suspend fun animateTo(entries: List<E>, maxValue: Double, spec: FiniteAnimationSpec<Float>) {
        val first = first ?: return
        val last = last ?: return
        val items = drawn
        coroutineScope {
            launch { first.animateTo(key(entries.first()).toFloat(), spec) }
            launch { last.animateTo(key(entries.last()).toFloat(), spec) }
            items.forEach { item ->
                val f = fractions[key(item.entry)] ?: return@forEach
                launch { f.animateTo(valueFraction(value(item.entry), maxValue), spec) }
            }
        }
        fractions.keys.retainAll(entries.mapTo(HashSet(), key))
    }
}

/**
 * The strip of [entries], each with its fraction of [maxValue]. With an [animationSpec], entry changes
 * animate as one strip matched by [key]: the shown keys scroll and zoom to the new ones and the fractions rescale.
 */
@Composable
internal fun <E> rememberEntryStrip(
    entries: List<E>,
    maxValue: Double,
    key: (E) -> Int,
    value: (E) -> Double?,
    animationSpec: FiniteAnimationSpec<Float>?
): EntryStrip<E> {
    if (animationSpec == null) {
        return remember(entries, maxValue) {
            val items = entries.mapIndexed { index, entry ->
                val fraction = valueFraction(value(entry), maxValue)
                StripItem(entry, index) { fraction }
            }
            EntryStrip(items, { 0f }, { (items.size - 1).toFloat() })
        }
    }
    val transitions = remember { StripTransitions(key, value) }
    // Bumped once removed entries have slid out, so they are dropped from the strip.
    var settled by remember { mutableIntStateOf(0) }
    val strip = remember(entries, maxValue, settled) { transitions.update(entries, maxValue) }
    LaunchedEffect(entries, maxValue) {
        transitions.animateTo(entries, maxValue, animationSpec)
        settled++
    }
    return strip
}

/**
 * A label under each slot of [strip], thinned out on narrow slots. [selectedIndex] points into the
 * entries, -1 for no selection; [label] gets whether the entry shows its extended label: the selected one,
 * or with none the latest, so the period stays readable.
 */
@Composable
internal fun <E> StripAxis(
    strip: EntryStrip<E>,
    selectedIndex: Int,
    viewportWidth: Dp,
    label: (E, Boolean) -> String,
    color: Color,
    selectedColor: Color
) {
    val items = strip.items
    val latestPosition = items.indexOfLast { it.index >= 0 }
    val selectedPosition = items.indexOfFirst { it.index >= 0 && it.index == selectedIndex }
    // Read while composing, so labels thin out progressively as the slots narrow.
    val start = strip.start()
    val visibleSlots = (strip.end() - start + 1f).coerceAtLeast(1f)
    val labelStep = axisLabelStep(viewportWidth / visibleSlots)
    Row(
        modifier = Modifier
            .stripWindow(strip)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { position, item ->
            if (!isAxisLabelShown(position, latestPosition, selectedPosition, labelStep)) {
                Spacer(modifier = Modifier.weight(1f))
                return@forEachIndexed
            }
            // Labels of slots outside the width are hidden rather than clipped, so edge labels can overflow.
            val center = position - start + 0.5f
            val isInside = center in 0f..visibleSlots
            val isSelected = position == selectedPosition
            val isExtended = isSelected || (selectedPosition < 0 && position == latestPosition)
            Text(
                text = label(item.entry, isExtended),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) selectedColor else color,
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
