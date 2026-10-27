package com.frafio.charts

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import java.text.NumberFormat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object PieChartDefaults {
    val Radius: Dp = 80.dp
    val ArcWidth: Dp = 14.dp
    val OffsetBetweenArcs: Dp = 4.dp
    val IconSize: Dp = 32.dp
    val SelectedIconSize: Dp = 36.dp
    val IconPadding: Dp = 6.dp
    val AnimationEasing: Easing = LinearOutSlowInEasing
    const val ChartAnimationDuration: Int = 1000

    /** How the arcs grow and shrink when the values change; pass `snap()` for a static chart. */
    val AnimationSpec: AnimationSpec<Float> = tween(ChartAnimationDuration, easing = AnimationEasing)

    /** Formats the values in the centre and in the arcs' descriptions, in the default locale. */
    val ValueFormatter: (Double) -> String = { NumberFormat.getInstance().format(it) }
}

/** An arc of [color], with [icon] tinted [iconColor] beside it; [labelColor] is used for [label] in the centre. */
data class PieChartItem(
    val value: Double,
    val label: String,
    val icon: Painter,
    val color: Color,
    val iconColor: Color,
    val labelColor: Color = color
)

/** How far a pressed, unselected arc and its icon grow towards their selected size. */
private const val PressedFactor = 0.5f

/** Degrees taken by the gap after an arc: its width plus [offsetPx], as an angle at [radiusPx]. */
internal fun arcGapDegrees(offsetPx: Float, arcWidthPx: Float, radiusPx: Float): Float =
    if (radiusPx > 0) {
        ((offsetPx + arcWidthPx) / radiusPx) * (180f / PI.toFloat())
    } else 0f

/** Sweep of each value's arc; a gap follows every positive arc unless it is the only one. */
internal fun arcSweeps(values: List<Double>, gapDegrees: Float): List<Float> {
    val count = values.count { it > 0.0 }
    val gap = if (count <= 1) 0f else gapDegrees
    val totalGap = gap * count
    val sum = values.sum()
    return values.map { value ->
        if (sum > 0) (360f - totalGap) * value.toFloat() / sum.toFloat() else 0f
    }
}

/**
 * A pie chart that keeps its own selection, starting with none; a new set of items clears it.
 * With nothing selected the centre shows [totalLabel] and the sum of the values.
 */
@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    items: List<PieChartItem>,
    totalLabel: String,
    valueFormatter: (Double) -> String = PieChartDefaults.ValueFormatter,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    animationSpec: AnimationSpec<Float> = PieChartDefaults.AnimationSpec,
    radius: Dp = PieChartDefaults.Radius,
    arcWidth: Dp = PieChartDefaults.ArcWidth,
    offsetBetweenArcs: Dp = PieChartDefaults.OffsetBetweenArcs,
    iconSize: Dp = PieChartDefaults.IconSize,
    selectedIconSize: Dp = PieChartDefaults.SelectedIconSize,
    iconPadding: Dp = PieChartDefaults.IconPadding
) {
    var selectedIndex by remember(items) { mutableIntStateOf(-1) }
    PieChart(
        modifier = modifier,
        items = items,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { selectedIndex = it },
        totalLabel = totalLabel,
        valueFormatter = valueFormatter,
        emptyColor = emptyColor,
        contentColor = contentColor,
        animationSpec = animationSpec,
        radius = radius,
        arcWidth = arcWidth,
        offsetBetweenArcs = offsetBetweenArcs,
        iconSize = iconSize,
        selectedIconSize = selectedIconSize,
        iconPadding = iconPadding
    )
}

/** A pie chart whose selection is hoisted; -1 selects nothing and shows [totalLabel] with the sum. */
@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    items: List<PieChartItem>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    totalLabel: String,
    valueFormatter: (Double) -> String = PieChartDefaults.ValueFormatter,
    emptyColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    animationSpec: AnimationSpec<Float> = PieChartDefaults.AnimationSpec,
    radius: Dp = PieChartDefaults.Radius,
    arcWidth: Dp = PieChartDefaults.ArcWidth,
    offsetBetweenArcs: Dp = PieChartDefaults.OffsetBetweenArcs,
    iconSize: Dp = PieChartDefaults.IconSize,
    selectedIconSize: Dp = PieChartDefaults.SelectedIconSize,
    iconPadding: Dp = PieChartDefaults.IconPadding
) {
    val density = LocalDensity.current
    val chartEntryOffset = remember(offsetBetweenArcs, arcWidth, radius, density) {
        with(density) { arcGapDegrees(offsetBetweenArcs.toPx(), arcWidth.toPx(), radius.toPx()) }
    }

    val selectedArcIndex = if (selectedIndex in items.indices) selectedIndex else -1

    val interactionSources = remember(items.size) { List(items.size) { MutableInteractionSource() } }
    val pressedStates = interactionSources.map { it.collectIsPressedAsState() }

    val arcWidthPx = with(density) { arcWidth.toPx() }
    val radiusPx = with(density) { radius.toPx() }

    val floatValues = remember(items, chartEntryOffset) {
        arcSweeps(items.map { it.value }, chartEntryOffset)
    }

    val animatedValues = floatValues.mapIndexed { index, value ->
        animateFloatAsState(
            targetValue = value,
            animationSpec = animationSpec,
            label = "arc_$index"
        )
    }

    val animatedOffsets = floatValues.mapIndexed { index, value ->
        val itemOffset = if (value > 0f && floatValues.count { it > 0f } > 1) chartEntryOffset else 0f
        animateFloatAsState(
            targetValue = itemOffset,
            animationSpec = animationSpec,
            label = "offset_$index"
        )
    }

    val animatedAlphas = floatValues.mapIndexed { index, value ->
        animateFloatAsState(
            targetValue = if (value > 0f) 1f else 0f,
            animationSpec = animationSpec,
            label = "alpha_$index"
        )
    }

    val selectionSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val selectionFactors = items.indices.map { index ->
        animateFloatAsState(
            targetValue = when {
                selectedArcIndex == index -> 1f
                pressedStates[index].value -> PressedFactor
                else -> 0f
            },
            animationSpec = selectionSpec,
            label = "selection_factor_$index"
        )
    }

    val emptyCircleAlpha by animateFloatAsState(
        targetValue = if (items.sumOf { it.value } == 0.0) 1f else 0f,
        animationSpec = animationSpec,
        label = "empty_alpha"
    )

    val chartPadding = selectedIconSize + iconPadding + (arcWidth * 0.6f) + 4.dp
    val totalChartSize = radius * 2f + chartPadding * 2f

    Box(
        modifier = modifier
            .size(totalChartSize)
            .selectableGroup(),
        contentAlignment = Alignment.Center
    ) {
        // Empty State Background
        if (emptyCircleAlpha > 0.001f) {
            Canvas(modifier = Modifier.size(radius * 2f)) {
                drawArc(
                    color = emptyColor.copy(alpha = emptyCircleAlpha),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(arcWidth.toPx())
                )
            }
        }

        // Arcs
        items.indices.forEach { index ->
            if (floatValues[index] > 0f || animatedValues[index].value > 0.01f) {
                PieChartArc(
                    contentDescription = stringResource(
                        R.string.charts_item_description,
                        items[index].label,
                        valueFormatter(items[index].value)
                    ),
                    isSelected = selectedArcIndex == index,
                    color = items[index].color,
                    iconPainter = items[index].icon,
                    iconOnColor = items[index].iconColor,
                    sweepState = animatedValues[index],
                    startAngleProvider = {
                        var start = -90f
                        for (i in 0 until index) {
                            start += animatedValues[i].value + animatedOffsets[i].value
                        }
                        start
                    },
                    alphaState = animatedAlphas[index],
                    selectionFactorState = selectionFactors[index],
                    radiusPx = radiusPx,
                    arcWidthPx = arcWidthPx,
                    iconSize = iconSize,
                    selectedIconSize = selectedIconSize,
                    iconPadding = iconPadding,
                    interactionSource = interactionSources[index],
                    onClick = { onSelectedIndexChange(index) }
                )
            }
        }

        // Center Content
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onSelectedIndexChange(-1) }
            )
        ) {
            val title = if (selectedArcIndex != -1) items[selectedArcIndex].label else totalLabel
            val valueText = if (selectedArcIndex != -1) items[selectedArcIndex].value else items.sumOf { it.value }

            Text(
                text = title,
                color = if (selectedArcIndex != -1) items[selectedArcIndex].labelColor else contentColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = valueFormatter(valueText),
                color = contentColor,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PieChartArc(
    contentDescription: String,
    isSelected: Boolean,
    color: Color,
    iconPainter: Painter,
    iconOnColor: Color,
    sweepState: State<Float>,
    startAngleProvider: () -> Float,
    alphaState: State<Float>,
    selectionFactorState: State<Float>,
    radiusPx: Float,
    arcWidthPx: Float,
    iconSize: Dp,
    selectedIconSize: Dp,
    iconPadding: Dp,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val tapExtraPx = with(density) { 24.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Hitbox Layer (Larger for easier interaction)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val sweep = sweepState.value
                    val start = startAngleProvider()
                    val factor = selectionFactorState.value
                    val strokePx = arcWidthPx * (1f + 0.2f * factor)
                    val radPx = radiusPx + (arcWidthPx * 0.15f * factor)

                    shape = ArcShape(start, sweep, strokePx + tapExtraPx, radPx)
                    clip = true
                    alpha = alphaState.value
                }
                .semantics { this.contentDescription = contentDescription }
                // The ripple is drawn by the visual arc below, from the same interaction source.
                .selectable(
                    selected = isSelected,
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick
                )
        ) {
            // Visual Arc & Ripple (Clipped to visible segment)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val sweep = sweepState.value
                        val start = startAngleProvider()
                        val factor = selectionFactorState.value
                        val strokePx = arcWidthPx * (1f + 0.2f * factor)
                        val radPx = radiusPx + (arcWidthPx * 0.15f * factor)

                        shape = ArcShape(start, sweep, strokePx, radPx)
                        clip = true
                    }
                    .background(color)
                    .indication(interactionSource, ripple())
            )
        }

        // Icon Layer
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val sweep = sweepState.value
                    val alphaValue = alphaState.value
                    if (sweep <= 0f || alphaValue <= 0f) {
                        alpha = 0f
                        return@graphicsLayer
                    }

                    val start = startAngleProvider()
                    val factor = selectionFactorState.value
                    val strokePx = arcWidthPx * (1f + 0.2f * factor)
                    val radPx = radiusPx + (arcWidthPx * 0.15f * factor)
                    val actualIconSize = lerp(iconSize, selectedIconSize, factor)
                    val iconRadiusPx = radPx + strokePx / 2 + (iconPadding + actualIconSize / 2).toPx()

                    val angleInRadians = ((start + sweep / 2) * PI / 180).toFloat()
                    translationX = iconRadiusPx * cos(angleInRadians)
                    translationY = iconRadiusPx * sin(angleInRadians)
                    // Scaled while drawing, so the size animation doesn't recompose.
                    scaleX = actualIconSize / iconSize
                    scaleY = scaleX
                    alpha = alphaValue
                }
                .size(iconSize)
                .align(Alignment.Center)
                .background(color, shape = CircleShape)
                .padding(6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                with(iconPainter) {
                    draw(size = size, colorFilter = ColorFilter.tint(iconOnColor))
                }
            }
        }
    }
}

@Composable
private fun previewItem(value: Double, label: String, @DrawableRes icon: Int, color: Color) =
    PieChartItem(value, label, painterResource(icon), color, iconColor = Color.White)

@Preview(showBackground = true)
@Composable
fun PieChartPreview() {
    MaterialTheme {
        val items = listOf(
            previewItem(1.0, "Housing", android.R.drawable.ic_menu_compass, Color(0xFF6750A4)),
            previewItem(1.0, "Groceries", android.R.drawable.ic_menu_gallery, Color(0xFF00897B)),
            previewItem(1.0, "Personal Care", android.R.drawable.ic_menu_call, Color(0xFFD32F2F))
        )
        PieChart(items = items, totalLabel = "Total")
    }
}

@Preview(showBackground = true)
@Composable
fun PieChartSingleEntryPreview() {
    MaterialTheme {
        val items = listOf(
            previewItem(100.0, "Housing", android.R.drawable.ic_menu_compass, Color(0xFF6750A4)),
            previewItem(0.0, "Other", android.R.drawable.ic_menu_gallery, Color(0xFF00897B))
        )
        PieChart(
            items = items,
            totalLabel = "Total",
            animationSpec = snap()
        )
    }
}
