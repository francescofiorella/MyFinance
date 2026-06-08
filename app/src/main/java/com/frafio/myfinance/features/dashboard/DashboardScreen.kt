package com.frafio.myfinance.features.dashboard

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.Expense
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.components.EmptyView
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.features.dashboard.components.AnnualBalanceCard
import com.frafio.myfinance.features.dashboard.components.BudgetIndicatorCard
import com.frafio.myfinance.features.dashboard.components.ExpensesByCategoryCard
import com.frafio.myfinance.features.dashboard.components.ExpensesCard
import com.frafio.myfinance.features.dashboard.components.MonthlyExpensesChartCard
import kotlin.collections.listOf

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val isListEmpty by viewModel.isListEmpty.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel.scrollToTop) {
        viewModel.scrollToTop.collectLatest {
            scrollState.animateScrollTo(0)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (isListEmpty) {
            true -> EmptyView(
                imageResLight = R.drawable.image_audit_amico,
                imageResDark = R.drawable.image_late_at_night_amico,
                messageRes = R.string.warning_home
            )

            false -> {
                DashboardContent(viewModel, scrollState)
            }

            null -> { /* Loading or Initial state */
            }
        }
    }
}

@Composable
fun DashboardContent(
    viewModel: DashboardViewModel,
    scrollState: ScrollState
) {
    val monthShown by viewModel.monthShown.collectAsStateWithLifecycle()
    val thisMonthSum by viewModel.thisMonthSum.collectAsStateWithLifecycle()
    val thisYearSum by viewModel.thisYearSum.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val todaySum by viewModel.todaySum.collectAsStateWithLifecycle()
    val balanceYear by viewModel.balanceYearShown.collectAsStateWithLifecycle()
    val incomesSum by viewModel.incomesSum.collectAsStateWithLifecycle()
    val expensesSum by viewModel.expensesSum.collectAsStateWithLifecycle()
    val barChartData by viewModel.barChartData.collectAsStateWithLifecycle()
    val isNextBarDateEnabled by viewModel.isNextBarChartDateEnabled.collectAsStateWithLifecycle()
    val pieExpenses by viewModel.pieChartExpenses.collectAsStateWithLifecycle()
    val pieDate by viewModel.pieChartDate.collectAsStateWithLifecycle()
    val monthlyShownInPie by viewModel.monthlyShownInPieChart.collectAsStateWithLifecycle()
    val isNextPieDateEnabled by viewModel.isNextPieChartDateEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 88.dp), // dashboard_bottom_margin
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BudgetIndicatorCard(
            monthShown = monthShown,
            thisMonthSum = thisMonthSum,
            thisYearSum = thisYearSum,
            monthlyBudget = monthlyBudget,
            onToggleMonthShown = { viewModel.toggleMonthShown(it) }
        )
        ExpensesCard(
            todaySum = todaySum,
            monthShown = monthShown,
            thisMonthSum = thisMonthSum,
            thisYearSum = thisYearSum
        )
        MonthlyExpensesChartCard(
            barChartData = barChartData,
            monthlyBudget = monthlyBudget,
            onPreviousDate = { viewModel.previousBarChartDate() },
            onNextDate = { viewModel.nextBarChartDate() },
            onToday = { viewModel.todayBarChartDate() },
            isNextDateEnabled = isNextBarDateEnabled
        )
        AnnualBalanceCard(
            balanceYear = balanceYear,
            incomesSum = incomesSum,
            expensesSum = expensesSum,
            onPreviousYear = { viewModel.previousBalanceYear() },
            onNextYear = { viewModel.nextBalanceYear() },
            onToday = { viewModel.todayBalanceYear() }
        )
        ExpensesByCategoryCard(
            expenses = pieExpenses,
            date = pieDate,
            monthlyShown = monthlyShownInPie,
            onSwitchData = { viewModel.switchPieChartData(it) },
            onPreviousDate = { viewModel.previousPieChartDate() },
            onNextDate = { viewModel.nextPieChartDate() },
            onToday = { viewModel.todayPieChartDate() },
            isNextDateEnabled = isNextPieDateEnabled
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MyFinanceTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            DashboardContent(
                monthShown = true,
                thisMonthSum = 450.0,
                thisYearSum = 5400.0,
                monthlyBudget = 1000.0,
                todaySum = 25.0,
                balanceYear = 2024,
                incomesSum = 2000.0,
                expensesSum = 1500.0,
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
                pieExpenses = emptyList(),
                pieDate = LocalDate.now(),
                monthlyShownInPie = true,
                scrollState = rememberScrollState(),
                onToggleMonthShown = {},
                onPreviousYear = {},
                onNextYear = {},
                onTodayAnnualBalance = {},
                onPreviousBarDate = {},
                onTodayBarDate = {},
                onNextBarDate = {},
                onSwitchPieData = {},
                onPreviousPieDate = {},
                onNextPieDate = {},
                onTodayPieData = {}
            )
        }
    }
}

@Composable
fun DashboardContent(
    monthShown: Boolean,
    thisMonthSum: Double,
    thisYearSum: Double,
    monthlyBudget: Double,
    todaySum: Double,
    balanceYear: Int,
    incomesSum: Double,
    expensesSum: Double,
    barChartData: List<BarChartEntry>,
    pieExpenses: List<Expense>,
    pieDate: LocalDate,
    monthlyShownInPie: Boolean,
    scrollState: ScrollState,
    onToggleMonthShown: (Boolean) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onTodayAnnualBalance: () -> Unit,
    onPreviousBarDate: () -> Unit,
    onNextBarDate: () -> Unit,
    onTodayBarDate: () -> Unit,
    isNextBarDateEnabled: Boolean = true,
    onSwitchPieData: (Boolean) -> Unit,
    onPreviousPieDate: () -> Unit,
    onNextPieDate: () -> Unit,
    onTodayPieData: () -> Unit,
    isNextPieDateEnabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 88.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BudgetIndicatorCard(
            monthShown = monthShown,
            thisMonthSum = thisMonthSum,
            thisYearSum = thisYearSum,
            monthlyBudget = monthlyBudget,
            onToggleMonthShown = onToggleMonthShown
        )
        ExpensesCard(
            todaySum = todaySum,
            monthShown = monthShown,
            thisMonthSum = thisMonthSum,
            thisYearSum = thisYearSum
        )
        MonthlyExpensesChartCard(
            barChartData = barChartData,
            monthlyBudget = monthlyBudget,
            onPreviousDate = onPreviousBarDate,
            onNextDate = onNextBarDate,
            onToday = onTodayBarDate,
            isNextDateEnabled = isNextBarDateEnabled
        )
        AnnualBalanceCard(
            balanceYear = balanceYear,
            incomesSum = incomesSum,
            expensesSum = expensesSum,
            onPreviousYear = onPreviousYear,
            onNextYear = onNextYear,
            onToday = onTodayAnnualBalance
        )
        ExpensesByCategoryCard(
            expenses = pieExpenses,
            date = pieDate,
            monthlyShown = monthlyShownInPie,
            onSwitchData = onSwitchPieData,
            onPreviousDate = onPreviousPieDate,
            onNextDate = onNextPieDate,
            onToday = onTodayPieData,
            isNextDateEnabled = isNextPieDateEnabled
        )
    }
}
