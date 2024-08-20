package com.frafio.myfinance.data.models

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun PieChart(
    data: List<Double>,
    radiusOuter: Dp = 60.dp,
    chartBarWidth: Dp = 10.dp,
    animDuration: Int = 500,
    animate: Boolean = true
) {
    val colorPrimary = colorResource(R.color.md_theme_primary)
    val colorSecondaryContainer = colorResource(R.color.md_theme_secondaryContainer)
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
    val defaultIcon = painterResource(R.drawable.ic_payments_myfinance)
    val icons = listOf(
        painterResource(R.drawable.ic_baseline_home),
        painterResource(R.drawable.ic_shopping_cart),
        painterResource(R.drawable.ic_self_care),
        painterResource(R.drawable.ic_theater_comedy),
        painterResource(R.drawable.ic_school),
        painterResource(R.drawable.ic_restaurant),
        painterResource(R.drawable.ic_vaccines),
        painterResource(R.drawable.ic_directions_transit),
        painterResource(R.drawable.ic_tag)
    )

    var text by remember { mutableStateOf(doubleToPriceWithoutDecimals(data.sum())) }
    var iconColor by remember { mutableStateOf(colorPrimary) }
    var iconPainter by remember { mutableStateOf(defaultIcon) }

    val totalSum = data.sum()
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
    val offset = 12 * data.count { v -> v > 0.0 }
    data.forEachIndexed { index, value ->
        val angle = (360 - offset) * value.toFloat() / totalSum.toFloat()
        angles[index] = Pair(angles[index].first, angles[index].first + angle)
        if (index < 8 && angle != 0F) {
            angles[index + 1] = Pair(angles[index].second + 12F, 0F)
        } else if (index < 8) {
            angles[index + 1] = Pair(angles[index].second, 0F)
        }
        floatValues.add(index, angle)
    }

    var animationPlayed by remember { mutableStateOf(!animate) }

    var lastValue = 0f

    // if you want to stabilize the Pie Chart you can use value -90f
    // 90f is used to complete 1/4 of the rotation
    val animateRotation by animateFloatAsState(
        targetValue = if (animationPlayed) 90f * 11f else 0f,
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
        contentAlignment = Alignment.Center
    ) {
        if (floatValues.sum() == 0F) {
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
                        detectTapGestures { offset ->
                            // Detect click on arcs here
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val clickedAngle = calculateAngle(offset, center)
                            // You can determine which arc is clicked based on this angle
                            // Check which arc was clicked and get the index
                            val clickedArcIndex = isArcClicked(clickedAngle, angles)

                            // Perform an action based on the clicked arc
                            clickedArcIndex?.let { i ->
                                text = doubleToPriceWithoutDecimals(data[i])
                                iconColor = colors[i]
                                iconPainter = icons[i]
                            }
                        }
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
                            style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Round),
                        )
                        lastValue += value + 12F
                    }

                }
            }
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = "Total",
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor, RoundedCornerShape(160.dp))
                    .padding(8.dp)
            )
            Text(
                text = text,
                color = colorResource(R.color.primary_text),
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.nunito)),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(PaddingValues(top = 5.dp))
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
