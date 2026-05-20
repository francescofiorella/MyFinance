package com.frafio.myfinance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.frafio.myfinance.data.model.BarChartEntry
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    entries: List<BarChartEntry>,
    referenceValue: Double? = null,
    barWidth: Dp = 40.dp,
    barPadding: Dp = 3.dp,
    barMaxHeight: Dp = 160.dp,
    onBarClick: (Int) -> Unit = {},
    resetIndicatorHook: Boolean = false
) {
    BoxWithConstraints(modifier = modifier) {
        val maxWidth = maxWidth
        val barItemWidth = barWidth + barPadding * 2
        val maxVisibleItems = (maxWidth / barItemWidth).toInt().coerceAtLeast(1)

        val startIndex = (entries.size - maxVisibleItems).coerceAtLeast(0)
        val visibleEntries = remember(entries, startIndex) { entries.drop(startIndex) }

        // Use rememberSaveable so that the selected index persists during screen rotation.
        var selectedIndex by rememberSaveable(resetIndicatorHook) {
            mutableIntStateOf(
                if (entries.isNotEmpty()) {
                    entries.size - 1
                } else {
                    -1
                }
            )
        }

        // Max value based on visible data for better scaling
        val maxValue = remember(visibleEntries, referenceValue) {
            // Ensure selectedIndex is within the visible range or valid for data
            if (entries.isNotEmpty() && (selectedIndex < startIndex || selectedIndex >= entries.size)) {
                selectedIndex = entries.size - 1
            }
            val max = visibleEntries.maxOfOrNull { it.value } ?: 0.0
            if (max == 0.0) 1.0 else max
        }

        val extendedLabel = remember(selectedIndex, visibleEntries) {
            if (selectedIndex == -1) {
                ""
            } else {
                val yearMonth =
                    YearMonth.of(entries[selectedIndex].year, entries[selectedIndex].month)

                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")

                yearMonth.format(formatter)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (selectedIndex == -1)
                        "" else doubleToPrice(entries[selectedIndex].value),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.padding(bottom = 5.dp),
                    text = extendedLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barMaxHeight),
                contentAlignment = Alignment.BottomStart
            ) {
                val interactionSources = remember(visibleEntries.size) {
                    List(visibleEntries.size) { MutableInteractionSource() }
                }
                ButtonGroup(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom,
                    overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                    }
                ) {
                    visibleEntries.forEachIndexed { index, entry ->
                        val originalIndex = index + startIndex
                        val isSelected = selectedIndex == originalIndex
                        val barHeightFraction =
                            (entry.value / maxValue).toFloat().coerceIn(0.01f, 1f)

                        customItem(
                            {
                                ChartBar(
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateWidth(interactionSources[index]),
                                    barWidth = barWidth,
                                    barPadding = barPadding,
                                    heightFraction = barHeightFraction,
                                    isSelected = isSelected,
                                    onClick = {
                                        selectedIndex = originalIndex
                                        onBarClick(originalIndex)
                                    },
                                    interactionSource = interactionSources[index]
                                )
                            },
                            {}
                        )
                    }
                }

                if (referenceValue != null && referenceValue > 0) {

                    val animatedHeightFraction by animateFloatAsState(
                        targetValue = (referenceValue / maxValue).toFloat(),
                        animationSpec = tween(durationMillis = 500),
                        label = "ReferenceHeight"
                    )
                    if (visibleEntries.isNotEmpty() && animatedHeightFraction in 0f..1f) {
                        HorizontalDivider(
                            modifier = Modifier
                                .zIndex(-1f)
                                .fillMaxWidth()
                                .padding(bottom = barMaxHeight * animatedHeightFraction),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (visibleEntries[visibleEntries.size - 1].value <= referenceValue) {
                            Text(
                                modifier = Modifier
                                    .zIndex(-1f)
                                    .fillMaxWidth()
                                    .offset {
                                        IntOffset(
                                            0,
                                            -(barMaxHeight * animatedHeightFraction).toPx()
                                                .roundToInt()
                                        )
                                    },
                                text = doubleToPriceWithoutDecimals(referenceValue),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                visibleEntries.forEachIndexed { index, entry ->
                    val originalIndex = index + startIndex
                    val isSelected = selectedIndex == originalIndex
                    val shortLabel = "%02d".format(entry.month).takeLast(2)
                    val extendedLabel = shortLabel + "/" + "%02d".format(entry.year).takeLast(2)
                    Text(
                        text = if (isSelected) extendedLabel else shortLabel,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
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
    }
}

@Composable
private fun ChartBar(
    modifier: Modifier = Modifier,
    barWidth: Dp = 40.dp,
    barPadding: Dp = 3.dp,
    heightFraction: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource
) {
    val animatedHeightFraction by animateFloatAsState(
        targetValue = heightFraction,
        animationSpec = tween(durationMillis = 500),
        label = "BarHeight"
    )
    ToggleButton(
        checked = isSelected,
        onCheckedChange = {
            if (it) onClick()
        },
        content = {},
        colors = ToggleButtonDefaults.toggleButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            checkedContainerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .fillMaxHeight(animatedHeightFraction)
            .padding(horizontal = barPadding)
            .width(barWidth),
        interactionSource = interactionSource
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun BarChartPreview() {
    val entries = listOf(
        BarChartEntry(100.0, 2024, 1),
        BarChartEntry(150.0, 2024, 2),
        BarChartEntry(80.0, 2024, 3),
        BarChartEntry(200.0, 2024, 4),
        BarChartEntry(120.0, 2024, 5),
        BarChartEntry(90.0, 2024, 6),
        BarChartEntry(180.0, 2024, 7),
        BarChartEntry(250.0, 2024, 8),
        BarChartEntry(60.0, 2024, 9),
        BarChartEntry(140.0, 2024, 10),
        BarChartEntry(110.0, 2024, 11),
        BarChartEntry(170.0, 2024, 12)
    )

    MyFinanceTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BarChart(entries = entries, referenceValue = 190.0)
        }
    }
}
