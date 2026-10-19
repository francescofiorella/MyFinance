package com.frafio.myfinance.features.dashboard.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.components.BarChart
import com.frafio.myfinance.core.components.ConnectedToggleRow
import com.frafio.myfinance.core.components.PeriodStepper
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun MonthlyExpensesChartCard(
    barChartData: List<BarChartEntry>,
    monthlyBudget: Double,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit,
    selectedRange: Int = 0,
    onSelectedRangeChange: (Int) -> Unit = {},
    shownCount: Int = barChartData.size,
    onVisibleCountChanged: (Int) -> Unit = {},
    isNextDateEnabled: Boolean = true,
    isPreviousDateEnabled: Boolean = true
) {
    // Index into barChartData; -1 selects the latest month.
    var selectedMonth by rememberSaveable { mutableIntStateOf(-1) }
    val shownData = barChartData.takeLast(shownCount)
    val firstShown = barChartData.size - shownData.size

    // Keep the selected month only while it is shown.
    LaunchedEffect(firstShown) {
        if (selectedMonth in 0 until firstShown) selectedMonth = -1
    }

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
                PeriodStepper(
                    onPrevious = onPreviousDate,
                    onNext = onNextDate,
                    onToday = {
                        onToday()
                        selectedMonth = -1
                    },
                    previousDescription = stringResource(R.string.previous_month),
                    nextDescription = stringResource(R.string.next_month),
                    todayDescription = stringResource(R.string.current_month),
                    isPreviousEnabled = isPreviousDateEnabled,
                    isNextEnabled = isNextDateEnabled
                )
            }

            ConnectedToggleRow(
                modifier = Modifier.padding(bottom = 16.dp),
                options = listOf(
                    stringResource(R.string.bar_chart_range_dynamic),
                    stringResource(R.string.bar_chart_range_6_months),
                    stringResource(R.string.bar_chart_range_1_year),
                    stringResource(R.string.bar_chart_range_2_years)
                ),
                selectedIndex = selectedRange,
                onSelect = onSelectedRangeChange
            )

            BarChart(
                entries = shownData,
                selectedIndex = if (selectedMonth >= firstShown) selectedMonth - firstShown else -1,
                onSelectedIndexChange = { selectedMonth = it + firstShown },
                referenceValue = monthlyBudget,
                onVisibleCountChanged = onVisibleCountChanged,
                entryAnimationSpec = tween(durationMillis = 500)
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
