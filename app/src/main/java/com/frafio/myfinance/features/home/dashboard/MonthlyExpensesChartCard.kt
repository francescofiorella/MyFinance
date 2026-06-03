package com.frafio.myfinance.features.home.dashboard

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.components.BarChart
import com.frafio.myfinance.core.theme.MyFinanceTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonthlyExpensesChartCard(
    barChartData: List<BarChartEntry>,
    monthlyBudget: Double,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit,
    isNextDateEnabled: Boolean = true
) {
    var resetBarChart by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.monthly_expenses),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val previousInteractionSource = remember { MutableInteractionSource() }
                val nextInteractionSource = remember { MutableInteractionSource() }
                val todayInteractionSource = remember { MutableInteractionSource() }
                ButtonGroup(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    overflowIndicator = { menuState ->
                        ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                    }
                ) {
                    customItem(
                        {
                            FilledTonalIconButton(
                                modifier = Modifier
                                    .size(IconButtonDefaults.smallContainerSize())
                                    .animateWidth(previousInteractionSource),
                                onClick = onPreviousDate,
                                shapes = IconButtonDefaults.shapes(
                                    shape = IconButtonDefaults.smallSquareShape,
                                ),
                                interactionSource = previousInteractionSource
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_arrow_left_filled),
                                    contentDescription = null,
                                )
                            }
                        },
                        {}
                    )
                    customItem(
                        {
                            FilledTonalIconButton(
                                modifier = Modifier
                                    .size(IconButtonDefaults.smallContainerSize())
                                    .animateWidth(nextInteractionSource),
                                onClick = onNextDate,
                                enabled = isNextDateEnabled,
                                shapes = IconButtonDefaults.shapes(
                                    shape = IconButtonDefaults.smallSquareShape,
                                ),
                                interactionSource = nextInteractionSource
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_keyboard_arrow_right_filled),
                                    contentDescription = null
                                )
                            }
                        },
                        {}
                    )
                    customItem(
                        {
                            FilledTonalIconButton(
                                modifier = Modifier
                                    .width(52.dp)
                                    .animateWidth(todayInteractionSource),
                                onClick = {
                                    onToday()
                                    resetBarChart = !resetBarChart
                                },
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = todayInteractionSource
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_today_filled),
                                    contentDescription = null,
                                )
                            }
                        },
                        {}
                    )
                }
            }

            BarChart(
                entries = barChartData,
                referenceValue = monthlyBudget,
                resetIndicatorHook = resetBarChart
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MonthlyExpensesChartCardPreview() {
    MyFinanceTheme {
        MonthlyExpensesChartCard(
            barChartData = listOf(
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
            ),
            monthlyBudget = 1000.0,
            onPreviousDate = {},
            onNextDate = {},
            onToday = {}
        )
    }
}
