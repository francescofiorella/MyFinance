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
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PieChart(
    data: List<Double>,
    radiusOuter: Dp = 80.dp,
    chartBarOffset: Dp = 1.dp,
    chartBarWidth: Dp = 12.dp,
    unselectedIconSize: Dp = 32.dp,
    selectedIconSize: Dp = 36.dp,
    iconDistance: Dp = 16.dp,
    animate: Boolean = true,
    animDuration: Int = 1000
) {
    val chartEntryOffset = with(LocalDensity.current) {
        chartBarOffset.toPx().roundToInt() + (chartBarWidth.toPx().roundToInt() / 4)
    }
    var selectedArc by remember(data) { mutableIntStateOf(-1) }

    val isDark = isSystemInDarkTheme()

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val secondaryContainerColor = MaterialTheme.colorScheme.surfaceVariant

    val painters = data.indices.map { painterResource(id = getCategoryIcon(it)) }

    val totalText = stringResource(R.string.total)
    val totalPriceText = doubleToPriceWithoutDecimals(data.sum())

    val floatValues = remember(data) {
        val values = mutableListOf<Float>()
        val count = data.count { v -> v > 0.0 }
        val offset = if (count <= 1) 0 else chartEntryOffset
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
        val itemOffset = if (value > 0f && floatValues.count { it > 0f } > 1) chartEntryOffset.toFloat() else 0f
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

    val emptyCircleAlpha by animateFloatAsState(
        targetValue = if (data.sum() == 0.0) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (animate) animDuration else 0,
            easing = LinearOutSlowInEasing
        ),
        label = "empty_alpha"
    )

    val targetOffset = if (data.count { v -> v > 0.0 } <= 1) 0 else chartEntryOffset

    Box(
        modifier = Modifier.padding(unselectedIconSize + iconDistance),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(radiusOuter * 2f)
                .pointerInput(data) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        val distance = sqrt((dx * dx + dy * dy).toDouble())

                        if (distance <= (radiusOuter + chartBarWidth).toPx() &&
                            distance >= (radiusOuter - chartBarWidth * 2).toPx()
                        ) {
                            var touchAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            while (touchAngle < -90f) touchAngle += 360f

                            var currentStartAngle = -90f
                            floatValues.forEachIndexed { index, value ->
                                if (value > 0f) {
                                    val endAngle = currentStartAngle + value
                                    if (touchAngle in (currentStartAngle - targetOffset / 2)..(endAngle + targetOffset / 2)) {
                                        selectedArc = index
                                    }
                                    currentStartAngle = endAngle + targetOffset
                                }
                            }
                        }
                    }
                }
        ) {
            // Draw background gray circle for empty state with fade animation
            if (emptyCircleAlpha > 0.001f) {
                drawArc(
                    color = secondaryContainerColor.copy(alpha = emptyCircleAlpha),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(chartBarWidth.toPx())
                )
            }

            var currentStartAngle = -90f
            animatedValues.forEachIndexed { index, animatedValueState ->
                val sweepAngle = animatedValueState.value
                val animatedOffset = animatedOffsets[index].value
                val alpha = animatedAlphas[index].value
                if (alpha > 0.001f && sweepAngle > 0.001f) {
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
                        color = getCategoryContainerColor(index, default = primaryColor, isDark = isDark).copy(alpha = alpha),
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        size = Size(radiusOuter.toPx() * 2f, radiusOuter.toPx() * 2f),
                        style = Stroke(width = strokeSize, cap = StrokeCap.Round),
                    )

                    val angleInRadians = ((currentStartAngle + sweepAngle / 2) * PI / 180).toFloat()
                    val iconRadius = radiusOuter.toPx() + strokeSize + iconDistance.toPx()
                    val topLeft = Offset(
                        x = center.x + iconRadius * cos(angleInRadians) - iconSize.toPx() / 2,
                        y = center.y + iconRadius * sin(angleInRadians) - iconSize.toPx() / 2
                    )

                    val paddingSize = 6.dp
                    val innerIconSize = iconSize - paddingSize * 2

                    drawRoundRect(
                        color = getCategoryContainerColor(index, default = primaryColor, isDark = isDark).copy(alpha = alpha),
                        size = Size(iconSize.toPx(), iconSize.toPx()),
                        cornerRadius = CornerRadius(160.dp.toPx()),
                        topLeft = topLeft
                    )

                    translate(
                        left = topLeft.x + paddingSize.toPx(),
                        top = topLeft.y + paddingSize.toPx()
                    ) {
                        with(painters[index]) {
                            draw(
                                size = Size(innerIconSize.toPx(), innerIconSize.toPx()),
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
