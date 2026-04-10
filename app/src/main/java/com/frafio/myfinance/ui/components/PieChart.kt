package com.frafio.myfinance.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
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
    radiusOuter: Dp = 80.dp,
    chartEntryOffset: Int = 12,
    chartBarWidth: Dp = 12.dp,
    unselectedIconSize: Dp = 32.dp,
    selectedIconSize: Dp = 36.dp,
    iconDistance: Dp = 16.dp,
    animate: Boolean = true,
    animDuration: Int = 1000,
    animAngle: Float = 45f
) {
    var selectedArc by remember(data) { mutableIntStateOf(-1) }

    val isDark = isSystemInDarkTheme()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer

    val painters: MutableList<Painter> = mutableListOf()
    for (i in 0..<data.size) {
        painters.add(painterResource(id = getCategoryIcon(i)))
    }

    val totalText = stringResource(R.string.total)

    val totalPriceText = doubleToPriceWithoutDecimals(data.sum())

    val floatValues = mutableListOf<Float>()
    val offset = if (data.count { v -> v > 0.0 } == 1) 0 else chartEntryOffset
    val totalOffset = offset * data.count { v -> v > 0.0 }
    data.forEach { value ->
        val angle = (360 - totalOffset) * value.toFloat() / data.sum().toFloat()
        floatValues.add(angle)
    }

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        if (animate) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = animDuration,
                    easing = LinearOutSlowInEasing
                )
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }

    val animateRotation = animationProgress.value * animAngle

    // Pie Chart using Canvas Arc
    Box(
        modifier = Modifier.padding(unselectedIconSize + iconDistance),
        contentAlignment = Alignment.Center
    ) {
        if (data.sum() == 0.0) {
            Canvas(
                modifier = Modifier
                    .size(radiusOuter * 2f)
            ) {
                drawArc(
                    color = secondaryContainerColor,
                    0F,
                    360F,
                    useCenter = false,
                    style = Stroke(chartBarWidth.toPx())
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .size(radiusOuter * 2f)
                    .rotate(animateRotation)
                    .pointerInput(data) {
                        detectTapGestures { tapOffset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            val distance = sqrt((dx * dx + dy * dy).toDouble())

                            if (distance <= (radiusOuter + chartBarWidth).toPx()
                                && distance >= (radiusOuter - chartBarWidth * 2).toPx()
                            ) {
                                // Get angle in -180 to 180 range
                                var touchAngle =
                                    Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                                // Normalize touchAngle to be positive if it falls behind the start
                                while (touchAngle < -90f - animAngle) touchAngle += 360f

                                var currentStartAngle = -90f - animAngle
                                floatValues.forEachIndexed { index, value ->
                                    if (value > 0f) {
                                        val endAngle = currentStartAngle + value

                                        // Check if the tap is within this specific slice
                                        if (touchAngle in (currentStartAngle - offset / 2)..(endAngle + offset / 2)) {
                                            selectedArc = index
                                        }
                                        currentStartAngle = endAngle + offset
                                    }
                                }
                            }
                        }
                    }
            ) {
                var currentStartAngle = -90f - animAngle
                // draw arc and icon for each data entry in Pie Chart
                floatValues.forEachIndexed { index, value ->
                    if (value != 0F) {
                        val strokeSize: Float
                        val iconSize: Dp
                        if (selectedArc == index) {
                            strokeSize = chartBarWidth.toPx() * 1.2f
                            iconSize = selectedIconSize
                        } else {
                            strokeSize = chartBarWidth.toPx()
                            iconSize = unselectedIconSize
                        }
                        drawArc(
                            color = getCategoryContainerColor(index, default = primaryColor, isDark = isDark),
                            currentStartAngle,
                            value,
                            useCenter = false,
                            size = Size(radiusOuter.toPx() * 2f, radiusOuter.toPx() * 2f),
                            style = Stroke(
                                width = strokeSize,
                                cap = StrokeCap.Round
                            ),
                        )
                        val angleInRadians = ((currentStartAngle + value / 2) * PI / 180).toFloat()
                        val topLeft = Offset(
                            -iconSize.toPx() / 2 + center.x + (radiusOuter.toPx() + strokeSize + iconDistance.toPx()) * cos(
                                angleInRadians
                            ),
                            -iconSize.toPx() / 2 + center.y + (radiusOuter.toPx() + strokeSize + iconDistance.toPx()) * sin(
                                angleInRadians
                            )
                        )
                        // Draw the icon background
                        val paddingSize = 6.dp
                        val innerIconSize = iconSize - paddingSize * 2
                        drawRoundRect(
                            color = getCategoryContainerColor(index, default = primaryColor, isDark = isDark),
                            size = Size(iconSize.toPx(), iconSize.toPx()),
                            cornerRadius = CornerRadius(160.dp.toPx()),
                            topLeft = topLeft
                        )
                        // Draw the icon
                        rotate(
                            degrees = -animAngle,
                            pivot = Offset(
                                topLeft.x + iconSize.toPx() / 2,
                                topLeft.y + iconSize.toPx() / 2
                            )
                        ) {
                            translate(
                                left = topLeft.x + paddingSize.toPx(),
                                top = topLeft.y + paddingSize.toPx()
                            ) {
                                with(painters[index]) {
                                    draw(
                                        size = Size(innerIconSize.toPx(), innerIconSize.toPx()),
                                        colorFilter = ColorFilter.tint(getCategoryOnContainerColor(index, default = surfaceColor, isDark = isDark))
                                    )
                                }
                            }
                        }
                        currentStartAngle += value + offset
                    }
                }
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
