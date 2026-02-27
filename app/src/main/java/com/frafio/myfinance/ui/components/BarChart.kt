package com.frafio.myfinance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToPrice

@Composable
fun BarChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    onBarClick: (Int) -> Unit = {}
) {
    // We don't use 'data' as a key here so that the selected index persists during "sliding"
    var selectedIndex by remember { mutableIntStateOf(if (data.isNotEmpty()) data.size - 5 else -1) }
    
    // Max value still needs to be recalculated when data changes for correct scaling
    val maxValue = remember(data) {
        if (selectedIndex == -1 && data.isNotEmpty()) selectedIndex = data.size - 1
        val max = data.maxOfOrNull { it } ?: 0.0
        // Use a multiplier (1.25) to ensure the highest bar leaves enough headroom for the popup
        if (max == 0.0) 1.0 else max * 1.25
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val chartHeight = maxHeight
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEachIndexed { index, value ->
                        val isSelected = selectedIndex == index
                        val barHeightFraction = (value / maxValue).toFloat().coerceIn(0.01f, 1f)
                        val indicatorOnRightCorner = index >= data.size / 2

                        ChartBar(
                            heightFraction = barHeightFraction,
                            isSelected = isSelected,
                            valueLabel = doubleToPrice(value),
                            showIndicator = isSelected,
                            indicatorOnRightCorner = indicatorOnRightCorner,
                            chartHeight = chartHeight,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedIndex = index
                                    onBarClick(index)
                                }
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEachIndexed { index, label ->
                val isSelected = selectedIndex == index
                Text(
                    text = if (isSelected) label else label.take(2),
                    fontSize = 11.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(align = Alignment.CenterHorizontally, unbounded = true),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Composable
private fun ChartBar(
    heightFraction: Float,
    isSelected: Boolean,
    valueLabel: String,
    showIndicator: Boolean,
    indicatorOnRightCorner: Boolean,
    chartHeight: Dp,
    modifier: Modifier = Modifier
) {
    val animatedHeightFraction by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = tween(durationMillis = 500),
        label = "BarHeight"
    )

    val barCornerRadius = dimensionResource(id = R.dimen.card_corner_radius)
    val popupRadius = 15.dp

    Box(
        modifier = modifier.zIndex(if (isSelected) 2f else 1f),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(animatedHeightFraction)
                .width(10.dp)
                .clip(RoundedCornerShape(barCornerRadius))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer)
        )

        if (showIndicator) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = (chartHeight * animatedHeightFraction) + 5.dp)
                    .width(10.dp)
                    .wrapContentSize(
                        align = if (indicatorOnRightCorner) Alignment.BottomEnd else Alignment.BottomStart,
                        unbounded = true
                    )
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = if (indicatorOnRightCorner)
                        RoundedCornerShape(topStart = popupRadius, topEnd = popupRadius, bottomStart = popupRadius, bottomEnd = 0.dp)
                    else
                        RoundedCornerShape(topStart = popupRadius, topEnd = popupRadius, bottomStart = 0.dp, bottomEnd = popupRadius),
                ) {
                    Text(
                        text = valueLabel,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 200)
@Composable
fun BarChartPreview() {
    val data = listOf(100.0, 150.0, 80.0, 200.0, 120.0, 90.0, 180.0, 250.0, 60.0, 140.0, 110.0, 170.0)
    val labels = listOf("01/24", "02/24", "03/24", "04/24", "05/24", "06/24", "07/24", "08/24", "09/24", "10/24", "11/24", "12/24")

    MyFinanceTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(
                data = data,
                labels = labels,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
