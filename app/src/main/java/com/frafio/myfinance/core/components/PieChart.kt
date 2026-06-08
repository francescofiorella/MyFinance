package com.frafio.myfinance.core.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.core.utils.getCategoryContainerColor
import com.frafio.myfinance.core.utils.getCategoryIcon
import com.frafio.myfinance.core.utils.getCategoryName
import com.frafio.myfinance.core.utils.getCategoryOnContainerColor
import com.frafio.myfinance.core.utils.getCategoryTextColor
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
    const val ArcSelectionAnimationDuration: Int = 100
}

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    entries: List<Double>,
    animate: Boolean = true,
    resetSelectionHook: Boolean = false,
    radius: Dp = PieChartDefaults.Radius,
    arcWidth: Dp = PieChartDefaults.ArcWidth,
    offsetBetweenArcs: Dp = PieChartDefaults.OffsetBetweenArcs,
    iconSize: Dp = PieChartDefaults.IconSize,
    selectedIconSize: Dp = PieChartDefaults.SelectedIconSize,
    iconPadding: Dp = PieChartDefaults.IconPadding,
    animDuration: Int = PieChartDefaults.ChartAnimationDuration
) {
    val density = LocalDensity.current
    val chartEntryOffset = remember(offsetBetweenArcs, arcWidth, radius, density) {
        with(density) {
            val radiusPx = radius.toPx()
            if (radiusPx > 0) {
                ((offsetBetweenArcs.toPx() + arcWidth.toPx()) / radiusPx) * (180f / PI.toFloat())
            } else 0f
        }
    }

    var selectedArcIndex by remember(entries, resetSelectionHook) { mutableIntStateOf(-1) }
    var pressedArcIndex by remember(entries, resetSelectionHook) { mutableIntStateOf(-1) }

    val interactionSources = remember(entries.size) { List(entries.size) { MutableInteractionSource() } }
    val isDark = isSystemInDarkTheme()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val arcWidthPx = with(density) { arcWidth.toPx() }
    val radiusPx = with(density) { radius.toPx() }

    val floatValues = remember(entries, chartEntryOffset) {
        val count = entries.count { it > 0.0 }
        val offset = if (count <= 1) 0f else chartEntryOffset
        val totalOffset = offset * count
        val sum = entries.sum()
        entries.map { value ->
            if (sum > 0) (360f - totalOffset) * value.toFloat() / sum.toFloat() else 0f
        }
    }

    val animatedValues = floatValues.mapIndexed { index, value ->
        animateFloatAsState(
            targetValue = value,
            animationSpec = tween(if (animate) animDuration else 0, easing = PieChartDefaults.AnimationEasing),
            label = "arc_$index"
        )
    }

    val animatedOffsets = floatValues.mapIndexed { index, value ->
        val itemOffset = if (value > 0f && floatValues.count { it > 0f } > 1) chartEntryOffset else 0f
        animateFloatAsState(
            targetValue = itemOffset,
            animationSpec = tween(if (animate) animDuration else 0, easing = PieChartDefaults.AnimationEasing),
            label = "offset_$index"
        )
    }

    val animatedAlphas = floatValues.mapIndexed { index, value ->
        animateFloatAsState(
            targetValue = if (value > 0f) 1f else 0f,
            animationSpec = tween(if (animate) animDuration else 0, easing = PieChartDefaults.AnimationEasing),
            label = "alpha_$index"
        )
    }

    val selectionFactors = entries.indices.map { index ->
        animateFloatAsState(
            targetValue = if (selectedArcIndex == index || pressedArcIndex == index) 1f else 0f,
            animationSpec = tween(PieChartDefaults.ArcSelectionAnimationDuration, easing = PieChartDefaults.AnimationEasing),
            label = "selection_factor_$index"
        )
    }

    val iconSelectionFactors = entries.indices.map { index ->
        animateFloatAsState(
            targetValue = if (selectedArcIndex == index) 1f else 0f,
            animationSpec = tween(PieChartDefaults.ArcSelectionAnimationDuration, easing = PieChartDefaults.AnimationEasing),
            label = "icon_selection_factor_$index"
        )
    }

    val emptyCircleAlpha by animateFloatAsState(
        targetValue = if (entries.sum() == 0.0) 1f else 0f,
        animationSpec = tween(if (animate) animDuration else 0, easing = PieChartDefaults.AnimationEasing),
        label = "empty_alpha"
    )

    val chartPadding = selectedIconSize + iconPadding + (arcWidth * 0.6f) + 4.dp
    val totalChartSize = radius * 2f + chartPadding * 2f

    Box(
        modifier = modifier.size(totalChartSize),
        contentAlignment = Alignment.Center
    ) {
        // Empty State Background
        if (emptyCircleAlpha > 0.001f) {
            Canvas(modifier = Modifier.size(radius * 2f)) {
                drawArc(
                    color = surfaceVariant.copy(alpha = emptyCircleAlpha),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(arcWidth.toPx())
                )
            }
        }

        // Arcs
        entries.indices.forEach { index ->
            PieChartArc(
                color = getCategoryContainerColor(index, default = primaryColor, isDark = isDark),
                iconPainter = painterResource(getCategoryIcon(index)),
                iconOnColor = getCategoryOnContainerColor(index, default = MaterialTheme.colorScheme.surface, isDark = isDark),
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
                iconSelectionFactorState = iconSelectionFactors[index],
                radiusPx = radiusPx,
                arcWidthPx = arcWidthPx,
                iconSize = iconSize,
                selectedIconSize = selectedIconSize,
                iconPadding = iconPadding,
                interactionSource = interactionSources[index],
                onPress = { pressedArcIndex = index },
                onRelease = {
                    selectedArcIndex = index
                    pressedArcIndex = -1
                },
                onCancel = { pressedArcIndex = -1 }
            )
        }

        // Center Content
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { selectedArcIndex = -1 }
            )
        ) {
            val title = if (selectedArcIndex != -1) stringResource(getCategoryName(selectedArcIndex)) else stringResource(R.string.total)
            val valueText = if (selectedArcIndex != -1) entries[selectedArcIndex] else entries.sum()

            Text(
                text = title,
                color = getCategoryTextColor(selectedArcIndex, MaterialTheme.colorScheme.onSurface, isDark = isDark),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = doubleToPriceWithoutDecimals(valueText),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PieChartArc(
    color: Color,
    iconPainter: Painter,
    iconOnColor: Color,
    sweepState: State<Float>,
    startAngleProvider: () -> Float,
    alphaState: State<Float>,
    selectionFactorState: State<Float>,
    iconSelectionFactorState: State<Float>,
    radiusPx: Float,
    arcWidthPx: Float,
    iconSize: Dp,
    selectedIconSize: Dp,
    iconPadding: Dp,
    interactionSource: MutableInteractionSource,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit
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
                .pointerInput(onPress, onRelease, onCancel) {
                    detectTapGestures(
                        onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            onPress()
                            val released = tryAwaitRelease()
                            if (released) {
                                onRelease()
                                interactionSource.emit(PressInteraction.Release(press))
                            } else {
                                onCancel()
                                interactionSource.emit(PressInteraction.Cancel(press))
                            }
                        }
                    )
                }
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
                    val iconFactor = iconSelectionFactorState.value
                    val strokePx = arcWidthPx * (1f + 0.2f * factor)
                    val radPx = radiusPx + (arcWidthPx * 0.15f * factor)
                    val actualIconSizePx = with(density) { (iconSize + (selectedIconSize - iconSize) * iconFactor).toPx() }
                    val iconRadiusPx = radPx + strokePx / 2 + with(density) { iconPadding.toPx() } + actualIconSizePx / 2

                    val angleInRadians = ((start + sweep / 2) * PI / 180).toFloat()
                    translationX = iconRadiusPx * cos(angleInRadians)
                    translationY = iconRadiusPx * sin(angleInRadians)
                    alpha = alphaValue
                }
                .size(lerp(iconSize, selectedIconSize, iconSelectionFactorState.value))
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

private class ArcShape(
    private val startAngle: Float,
    private val sweepAngle: Float,
    private val strokeWidthPx: Float,
    private val radiusPx: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        if (sweepAngle <= 0f) return Outline.Generic(Path())
        val path = Path()
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = radiusPx + strokeWidthPx / 2f
        val innerRadius = radiusPx - strokeWidthPx / 2f

        val outerRect = Rect(center.x - outerRadius, center.y - outerRadius, center.x + outerRadius, center.y + outerRadius)
        val innerRect = Rect(center.x - innerRadius, center.y - innerRadius, center.x + innerRadius, center.y + innerRadius)

        val endAngle = startAngle + sweepAngle
        val startAngleRad = Math.toRadians(startAngle.toDouble())
        val endAngleRad = Math.toRadians(endAngle.toDouble())

        val startCapCenter = Offset(
            center.x + radiusPx * cos(startAngleRad).toFloat(),
            center.y + radiusPx * sin(startAngleRad).toFloat()
        )
        val endCapCenter = Offset(
            center.x + radiusPx * cos(endAngleRad).toFloat(),
            center.y + radiusPx * sin(endAngleRad).toFloat()
        )
        val capRadius = strokeWidthPx / 2f
        val startCapRect = Rect(startCapCenter.x - capRadius, startCapCenter.y - capRadius, startCapCenter.x + capRadius, startCapCenter.y + capRadius)
        val endCapRect = Rect(endCapCenter.x - capRadius, endCapCenter.y - capRadius, endCapCenter.x + capRadius, endCapCenter.y + capRadius)

        path.arcTo(outerRect, startAngle, sweepAngle, true)
        path.arcTo(endCapRect, endAngle, 180f, false)
        path.arcTo(innerRect, endAngle, -sweepAngle, false)
        path.arcTo(startCapRect, startAngle + 180f, 180f, false)
        path.close()

        return Outline.Generic(path)
    }
}

@Preview(showBackground = true)
@Composable
fun PieChartPreview() {
    MyFinanceTheme {
        PieChart(
            entries = listOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
        )
    }
}
