package com.frafio.myfinance.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.utils.getCategoryContainerColor
import com.frafio.myfinance.utils.getCategoryIcon
import com.frafio.myfinance.utils.getCategoryName
import com.frafio.myfinance.utils.getCategoryOnContainerColor
import com.frafio.myfinance.utils.getCategoryTextColor
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PieChart(
    data: List<Double>,
    radius: Dp = 80.dp,
    barWidth: Dp = 14.dp,
    offsetBetweenBars: Dp = 4.dp,
    iconSize: Dp = 32.dp,
    selectedIconSize: Dp = 36.dp,
    iconPadding: Dp = 6.dp,
    animate: Boolean = true,
    animDuration: Int = 1000
) {
    val density = LocalDensity.current
    val chartEntryOffset = remember(offsetBetweenBars, barWidth, radius, density) {
        with(density) {
            val radiusPx = radius.toPx()
            if (radiusPx > 0) {
                ((offsetBetweenBars.toPx() + barWidth.toPx()) / radiusPx) * (180f / PI.toFloat())
            } else 0f
        }
    }
    var selectedArc by remember(data) { mutableIntStateOf(-1) }
    var pressedArc by remember(data) { mutableIntStateOf(-1) }

    val isDark = isSystemInDarkTheme()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val secondaryContainerColor = MaterialTheme.colorScheme.surfaceVariant

    val painters = data.indices.map { painterResource(id = getCategoryIcon(it)) }

    val totalText = stringResource(R.string.total)
    val totalPriceText = doubleToPriceWithoutDecimals(data.sum())

    val floatValues = remember(data, chartEntryOffset) {
        val values = mutableListOf<Float>()
        val count = data.count { v -> v > 0.0 }
        val offset = if (count <= 1) 0f else chartEntryOffset
        val totalOffset = offset * count
        val sum = data.sum()
        data.forEach { value ->
            val angle = if (sum > 0) (360f - totalOffset) * value.toFloat() / sum.toFloat() else 0f
            values.add(angle)
        }
        values
    }

    val animatedValues = floatValues.mapIndexed { index, value ->
        animateFloatAsState(
            targetValue = value,
            animationSpec = tween(
                durationMillis = if (animate) animDuration else 0,
                easing = LinearOutSlowInEasing
            ),
            label = "arc_$index"
        )
    }

    val animatedOffsets = floatValues.mapIndexed { index, value ->
        val itemOffset = if (value > 0f && floatValues.count { it > 0f } > 1) chartEntryOffset else 0f
        animateFloatAsState(
            targetValue = itemOffset,
            animationSpec = tween(
                durationMillis = if (animate) animDuration else 0,
                easing = LinearOutSlowInEasing
            ),
            label = "offset_$index"
        )
    }

    val animatedAlphas = floatValues.mapIndexed { index, value ->
        animateFloatAsState(
            targetValue = if (value > 0f) 1f else 0f,
            animationSpec = tween(
                durationMillis = if (animate) animDuration else 0,
                easing = LinearOutSlowInEasing
            ),
            label = "alpha_$index"
        )
    }

    val selectionFactors = data.indices.map { index ->
        animateFloatAsState(
            targetValue = if (selectedArc == index || pressedArc == index) 1f else 0f,
            animationSpec = tween(
                durationMillis = 50,
                easing = LinearOutSlowInEasing
            ),
            label = "selection_factor_$index"
        )
    }

    val emptyCircleAlpha by animateFloatAsState(
        targetValue = if (data.sum() == 0.0) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (animate) animDuration else 0,
            easing = LinearOutSlowInEasing
        ),
        label = "empty_alpha"
    )

    val targetOffset = if (data.count { v -> v > 0.0 } <= 1) 0f else chartEntryOffset
    val chartPadding = selectedIconSize + iconPadding + (barWidth * 0.6f) + 4.dp

    Box(
        modifier = Modifier.padding(chartPadding),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(radius * 2f)
                .pointerInput(data) {
                    detectTapGestures(
                        onPress = { tapOffset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            val distance = sqrt((dx * dx + dy * dy).toDouble())

                            if (distance <= (radius + barWidth * 2).toPx() &&
                                distance >= (radius - barWidth * 2).toPx()
                            ) {
                                var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                while (touchAngle < -90f) touchAngle += 360f

                                var currentStartAngle = -90f
                                var foundIndex = -1
                                floatValues.forEachIndexed { index, value ->
                                    if (value > 0f) {
                                        val endAngle = currentStartAngle + value
                                        if (touchAngle in (currentStartAngle - targetOffset / 2)..(endAngle + targetOffset / 2)) {
                                            foundIndex = index
                                        }
                                        currentStartAngle = endAngle + targetOffset
                                    }
                                }

                                if (foundIndex != -1) {
                                    pressedArc = foundIndex
                                    val released = tryAwaitRelease()
                                    if (released) {
                                        selectedArc = if (selectedArc == foundIndex) -1 else foundIndex
                                    }
                                    pressedArc = -1
                                }
                            }
                        }
                    )
                }
        )
{
            // Draw background gray circle for empty state with fade animation
            if (emptyCircleAlpha > 0.001f) {
                drawArc(
                    color = secondaryContainerColor.copy(alpha = emptyCircleAlpha),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(barWidth.toPx())
                )
            }

            var currentStartAngle = -90f
            animatedValues.forEachIndexed { index, animatedValueState ->
                val sweepAngle = animatedValueState.value
                val animatedOffset = animatedOffsets[index].value
                val alpha = animatedAlphas[index].value
                val selectionFactor = selectionFactors[index].value

                if (alpha > 0.001f && sweepAngle > 0.001f) {
                    val strokeSizePx = barWidth.toPx() * (1f + 0.2f * selectionFactor)
                    val actualIconSizePx = iconSize.toPx() + (selectedIconSize.toPx() - iconSize.toPx()) * selectionFactor
                    
                    val currentRadiusPx = radius.toPx() + (barWidth.toPx() * 0.15f * selectionFactor)
                    
                    drawArc(
                        color = getCategoryContainerColor(index, default = primaryColor, isDark = isDark).copy(alpha = alpha),
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - currentRadiusPx, center.y - currentRadiusPx),
                        size = Size(currentRadiusPx * 2f, currentRadiusPx * 2f),
                        style = Stroke(width = strokeSizePx, cap = StrokeCap.Round),
                    )

                    val angleInRadians = ((currentStartAngle + sweepAngle / 2) * PI / 180).toFloat()
                    val iconRadius = currentRadiusPx + strokeSizePx / 2 + iconPadding.toPx() + actualIconSizePx / 2
                    val topLeft = Offset(
                        x = center.x + iconRadius * cos(angleInRadians) - actualIconSizePx / 2,
                        y = center.y + iconRadius * sin(angleInRadians) - actualIconSizePx / 2
                    )

                    val paddingSizePx = 6.dp.toPx()
                    val innerIconSizePx = actualIconSizePx - paddingSizePx * 2

                    drawRoundRect(
                        color = getCategoryContainerColor(index, default = primaryColor, isDark = isDark).copy(alpha = alpha),
                        size = Size(actualIconSizePx, actualIconSizePx),
                        cornerRadius = CornerRadius(160.dp.toPx()),
                        topLeft = topLeft
                    )

                    translate(
                        left = topLeft.x + paddingSizePx,
                        top = topLeft.y + paddingSizePx
                    ) {
                        with(painters[index]) {
                            draw(
                                size = Size(innerIconSizePx, innerIconSizePx),
                                colorFilter = ColorFilter.tint(getCategoryOnContainerColor(index, default = surfaceColor, isDark = isDark)),
                                alpha = alpha
                            )
                        }
                    }
                }
                currentStartAngle += sweepAngle + animatedOffset
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { selectedArc = -1 }
            )
        ) {
            Text(
                text = if (selectedArc != -1) stringResource(id = getCategoryName(selectedArc)) else totalText,
                color = getCategoryTextColor(selectedArc, MaterialTheme.colorScheme.onSurface, isDark = isDark),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (selectedArc != -1) doubleToPriceWithoutDecimals(data[selectedArc]) else totalPriceText,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PieChartPreview() {
    MyFinanceTheme {
        PieChart(
            data = listOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0)
        )
    }
}
