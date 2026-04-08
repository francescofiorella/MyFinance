package com.frafio.myfinance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun ProgressBar(
    value: Double,
    maxValue: Double,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    errorColor: Color = MaterialTheme.colorScheme.error,
    labelContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    labelContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    barHeight: Dp = 10.dp,
    showLabel: Boolean = true
) {
    if (maxValue <= 0.0) return

    val rawPercentage = (value / maxValue).toFloat()
    val displayPercentage = rawPercentage.coerceIn(0.08f, 1f)
    val percentageString = "${(rawPercentage * 100).toInt()}%"

    val animatedProgress by animateFloatAsState(
        targetValue = displayPercentage,
        label = "ProgressBar"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showLabel) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart
            ) {
                // Label popup
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .wrapContentWidth(Alignment.End)
                        .padding(bottom = 4.dp),
                    color = labelContainerColor,
                    shape = RoundedCornerShape(
                        topStart = 15.dp,
                        topEnd = 15.dp,
                        bottomStart = 15.dp,
                        bottomEnd = 0.dp
                    )
                ) {
                    Text(
                        text = percentageString,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelContentColor
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .background(containerColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(if (rawPercentage < 1f) progressColor else errorColor)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressBarPreview() {
    MyFinanceTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProgressBar(value = 50.0, maxValue = 100.0)
            ProgressBar(value = 100.0, maxValue = 100.0)
            ProgressBar(value = 30.0, maxValue = 100.0, showLabel = false)
        }
    }
}
