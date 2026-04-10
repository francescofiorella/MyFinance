package com.frafio.myfinance.ui.features.home.dashboard

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.data.model.BarChartEntry
import com.frafio.myfinance.ui.components.EmptyView
import com.frafio.myfinance.ui.home.dashboard.DashboardViewModel
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import kotlin.collections.listOf

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val isListEmpty by viewModel.isListEmpty.collectAsState()
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardContent(
    viewModel: DashboardViewModel,
    scrollState: ScrollState
) {
    val monthShown by viewModel.monthShown.collectAsState()
    val thisMonthSum by viewModel.thisMonthSum.collectAsState()
    val thisYearSum by viewModel.thisYearSum.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val todaySum by viewModel.todaySum.collectAsState()
    val balanceYear by viewModel.balanceYearShown.collectAsState()
    val incomesSum by viewModel.incomesSum.collectAsState()
    val expensesSum by viewModel.expensesSum.collectAsState()
    val barChartData by viewModel.barChartData.collectAsState()
    val pieExpenses by viewModel.pieChartExpenses.collectAsState()
    val pieDate by viewModel.pieChartDate.collectAsState()
    val monthlyShownInPie by viewModel.monthlyShownInPieChart.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 88.dp) // dashboard_bottom_margin
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
            onToday = { viewModel.todayBarChartDate() }
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
            onToday = { viewModel.todayPieChartDate() }
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
    onSwitchPieData: (Boolean) -> Unit,
    onPreviousPieDate: () -> Unit,
    onNextPieDate: () -> Unit,
    onTodayPieData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 88.dp)
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
            onToday = onTodayBarDate
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
            onToday = onTodayPieData
        )
    }
}
