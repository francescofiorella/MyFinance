package com.frafio.myfinance.features.dashboard.components

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
import com.frafio.myfinance.core.components.MonthlyRangeBarChart
import com.frafio.myfinance.core.components.PeriodStepper
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import java.time.LocalDate

@Composable
fun MonthlyExpensesChartCard(
    barChartData: List<MonthlyTotal>,
    monthlyBudget: Double,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit,
    selectedRange: Int = 0,
    onSelectedRangeChange: (Int) -> Unit = {},
    shownCount: Int = barChartData.size,
    onVisibleCountChanged: (Int) -> Unit = {},
    availableRanges: List<Int> = ALL_MONTH_RANGES,
    trackingStart: LocalDate? = null,
    isNextDateEnabled: Boolean = true,
    isPreviousDateEnabled: Boolean = true
) {
    // Index into barChartData; -1 selects the latest month.
    var selectedMonth by rememberSaveable { mutableIntStateOf(-1) }

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
                    .fillMaxWidth(),
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

            MonthlyRangeBarChart(
                totals = barChartData,
                shownCount = shownCount,
                selectedRange = selectedRange,
                onSelectedRangeChange = onSelectedRangeChange,
                selectedMonth = selectedMonth,
                onSelectedMonthChange = { selectedMonth = it },
                availableRanges = availableRanges,
                trackingStart = trackingStart,
                referenceValue = monthlyBudget,
                onVisibleCountChanged = onVisibleCountChanged
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
                MonthlyTotal(100.0, 2024, 1),
                MonthlyTotal(150.0, 2024, 2),
                MonthlyTotal(80.0, 2024, 3),
                MonthlyTotal(200.0, 2024, 4),
                MonthlyTotal(120.0, 2024, 5),
                MonthlyTotal(90.0, 2024, 6),
                MonthlyTotal(180.0, 2024, 7),
                MonthlyTotal(250.0, 2024, 8),
                MonthlyTotal(60.0, 2024, 9),
                MonthlyTotal(140.0, 2024, 10),
                MonthlyTotal(110.0, 2024, 11),
                MonthlyTotal(170.0, 2024, 12)
            ),
            monthlyBudget = 1000.0,
            onPreviousDate = {},
            onNextDate = {},
            onToday = {}
        )
    }
}
