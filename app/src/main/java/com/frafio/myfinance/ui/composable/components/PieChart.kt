package com.frafio.myfinance.ui.composable.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Preview(showBackground = true)
@Composable
fun PieChartPreview() {
    PieChart(
        data = listOf(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
        isEmpty = false
    )
}

@Composable
fun PieChart(
    data: List<Double>,
    isEmpty: Boolean,
    radiusOuter: Dp = 80.dp,
    chartEntryOffset: Int = 9,
    chartBarWidth: Dp = 10.dp,
    animDuration: Int = 500,
    animate: Boolean = true
) {
    val colorPrimary = colorResource(R.color.md_theme_primary)
    val colorSecondaryContainer = colorResource(R.color.md_theme_secondaryContainer)
    val colorInvertedPrimaryText = colorResource(R.color.inverted_primary_text)
    val colors = listOf(
        colorResource(R.color.red_500),
        colorResource(R.color.purple_500),
        colorResource(R.color.indigo_500),
        colorResource(R.color.light_blue_500),
        colorResource(R.color.teal_500),
        colorResource(R.color.light_green_500),
        colorResource(R.color.yellow_500),
        colorResource(R.color.orange_500),
        colorResource(R.color.brown_500)
    )
    val painters = listOf(
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_home_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_shopping_cart_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_self_care_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_theater_comedy_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_school_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_restaurant_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_vaccines_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_directions_subway_filled)),
        rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_grid_3x3_filled))
    )

    val defaultSelectedText = stringResource(R.string.total)
    val categories = stringArrayResource(R.array.categories)

    val iconSize = 24.dp
    val iconDistance = 15.dp

    var text by remember { mutableStateOf(doubleToPriceWithoutDecimals(data.sum())) }
    var selectedText by remember { mutableStateOf(defaultSelectedText) }
    var selectedTextColor by remember { mutableStateOf(colorPrimary) }
    var strokeSizes by remember {
        mutableStateOf(
            listOf(
                chartBarWidth,
                chartBarWidth,
                chartBarWidth,
                chartBarWidth,
                chartBarWidth,
                chartBarWidth,
                chartBarWidth,
                chartBarWidth,
                chartBarWidth
            )
        )
    }
    var iconSizes by remember {
        mutableStateOf(
            listOf(
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
                iconSize
            )
        )
    }

    val floatValues = mutableListOf<Float>()
    val angles = mutableListOf(
        Pair(0F, 0F),
        Pair(0F, 0F),
        Pair(0F, 0F),
        Pair(0F, 0F),
        Pair(0F, 0F),
        Pair(0F, 0F),
        Pair(0F, 0F),
        Pair(0F, 0F),
        Pair(0F, 0F)
    )

    // To set the value of each Arc according to
    // the value given in the data, we have used a simple formula.
    val offset = if (data.count { v -> v > 0.0 } == 1) 0 else chartEntryOffset
    val totalOffset = offset * data.count { v -> v > 0.0 }
    data.forEachIndexed { index, value ->
        val angle = (360 - totalOffset) * value.toFloat() / data.sum().toFloat()
        angles[index] = Pair(angles[index].first, angles[index].first + angle)
        if (index < 8 && angle != 0F) {
            angles[index + 1] = Pair(angles[index].second + offset, 0F)
        } else if (index < 8) {
            angles[index + 1] = Pair(angles[index].second, 0F)
        }
        floatValues.add(index, angle)
    }

    var animationPlayed by remember { mutableStateOf(!animate) }

    var lastValue = -135f
    val animateRotation by animateFloatAsState(
        targetValue = if (animationPlayed) 45f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = 0,
            easing = LinearOutSlowInEasing
        ),
        label = "Rotation animation"
    )

    // to play the animation only once when the function is Created or Recomposed
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    // Pie Chart using Canvas Arc
    Box(
        modifier = Modifier.padding(iconSize + iconDistance),
        contentAlignment = Alignment.Center
    ) {
        if (isEmpty) {
            Canvas(
                modifier = Modifier
                    .size(radiusOuter * 2f)
            ) {
                drawArc(
                    color = colorSecondaryContainer,
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
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // Detect click on arcs here
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val clickedAngle = calculateAngle(offset, center)
                                // You can determine which arc is clicked based on this angle
                                // Check which arc was clicked and get the index
                                val clickedArcIndex = isArcClicked(clickedAngle + 135f, angles)

                                // Perform an action based on the clicked arc
                                clickedArcIndex?.let { i ->
                                    selectedText = categories[i]
                                    selectedTextColor = colors[i]
                                    text = doubleToPriceWithoutDecimals(data[i])
                                    val newStrokeSizes = mutableListOf(
                                        chartBarWidth,
                                        chartBarWidth,
                                        chartBarWidth,
                                        chartBarWidth,
                                        chartBarWidth,
                                        chartBarWidth,
                                        chartBarWidth,
                                        chartBarWidth,
                                        chartBarWidth
                                    )
                                    newStrokeSizes[i] = chartBarWidth * 1.2f
                                    strokeSizes = newStrokeSizes
                                    val newIconSizes = mutableListOf(
                                        iconSize,
                                        iconSize,
                                        iconSize,
                                        iconSize,
                                        iconSize,
                                        iconSize,
                                        iconSize,
                                        iconSize,
                                        iconSize
                                    )
                                    newIconSizes[i] = 30.dp
                                    iconSizes = newIconSizes
                                }
                            }
                        )
                    }
            ) {
                // draw each Arc for each data entry in Pie Chart
                floatValues.forEachIndexed { index, value ->
                    if (value != 0F) {
                        drawArc(
                            color = colors[index],
                            lastValue,
                            value,
                            useCenter = false,
                            size = Size(radiusOuter.toPx() * 2f, radiusOuter.toPx() * 2f),
                            style = Stroke(strokeSizes[index].toPx(), cap = StrokeCap.Round),
                        )
                        val angleInRadians = ((lastValue + value / 2) * PI / 180).toFloat()
                        val topLeft = Offset(
                            -iconSizes[index].toPx() / 2 + center.x + (radiusOuter.toPx() + strokeSizes[index].toPx() + iconDistance.toPx()) * cos(
                                angleInRadians
                            ),
                            -iconSizes[index].toPx() / 2 + center.y + (radiusOuter.toPx() + strokeSizes[index].toPx() + iconDistance.toPx()) * sin(
                                angleInRadians
                            )
                        )
                        // Draw the background fo the icons
                        val paddingSize = 6.dp
                        val innerIconSize = iconSizes[index] - paddingSize * 2
                        drawRoundRect(
                            color = colors[index],
                            size = Size(iconSizes[index].toPx(), iconSizes[index].toPx()),
                            cornerRadius = CornerRadius(160.dp.toPx()),
                            topLeft = topLeft
                        )
                        // Draw the icons
                        rotate(
                            degrees = -45F,
                            pivot = Offset(
                                topLeft.x + iconSizes[index].toPx() / 2 ,
                                topLeft.y + iconSizes[index].toPx() / 2
                            )
                        ) {
                            translate(
                                left = topLeft.x + paddingSize.toPx(),
                                top = topLeft.y + paddingSize.toPx()
                            ) {
                                with(painters[index]) {
                                    draw(
                                        size = Size(innerIconSize.toPx(), innerIconSize.toPx()),
                                        colorFilter = ColorFilter.tint(colorInvertedPrimaryText)
                                    )
                                }
                            }
                        }
                        lastValue += value + offset
                    }
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedText,
                color = selectedTextColor,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.nunito_bold)),
                textAlign = TextAlign.Center
            )
            Text(
                text = text,
                color = colorResource(R.color.md_theme_onSurface),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.nunito)),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun calculateAngle(tapOffset: Offset, center: Offset): Float {
    // Calculate the difference between the tap position and the center of the canvas
    val deltaX = tapOffset.x - center.x
    val deltaY = tapOffset.y - center.y

    // Convert the (x, y) coordinates into an angle in radians
    val radians = atan2(deltaY, deltaX)

    // Convert radians to degrees
    var angle = radians * 180 / PI

    // atan2 gives angles from -180 to 180 degrees, so convert it to 0 to 360 degrees
    if (angle < 0) {
        angle += 360f
    }

    return angle.toFloat()
}

fun isArcClicked(angle: Float, angles: List<Pair<Float, Float>>): Int? {
    for ((index, arc) in angles.withIndex()) {
        val (arcStartAngle, arcEndAngle) = arc

        // Handle the case where the arc might wrap around 0 degrees
        if (arcStartAngle <= arcEndAngle) {
            // Normal case: arc is within 0 to 360 degrees range
            if (angle in arcStartAngle..arcEndAngle) {
                return index
            }
        } else {
            // Case where the arc crosses the 0 degree line
            if (angle >= arcStartAngle || angle <= arcEndAngle) {
                return index
            }
        }
    }
    // If no arc matches, return null
    return null
}
