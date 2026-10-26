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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.Expense
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.core.components.EmptyView
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.features.dashboard.components.AnnualBalanceCard
import com.frafio.myfinance.features.dashboard.components.BudgetIndicatorCard
import com.frafio.myfinance.features.dashboard.components.ExpensesByCategoryCard
import com.frafio.myfinance.features.dashboard.components.ExpensesCard
import com.frafio.myfinance.features.dashboard.components.MonthlyExpensesChartCard
import kotlin.collections.listOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
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
                modifier = Modifier.fillMaxSize(),
                image = R.drawable.image_investment_data_cuate,
                imageDark = R.drawable.image_investment_data_cuate,
                message = R.string.warning_home
            )

            false -> {
                DashboardContent(viewModel, scrollState)
            }

            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
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
    val annualBalanceData by viewModel.annualBalanceData.collectAsStateWithLifecycle()
    val barChartData by viewModel.barChartData.collectAsStateWithLifecycle()
    val barChartRange by viewModel.barChartRange.collectAsStateWithLifecycle()
    val barChartShownCount by viewModel.barChartShownCount.collectAsStateWithLifecycle()
    val barChartTrackingStart by viewModel.barChartTrackingStart.collectAsStateWithLifecycle()
    val isNextBarDateEnabled by viewModel.isNextBarChartDateEnabled.collectAsStateWithLifecycle()
    val isPreviousBarDateEnabled by viewModel.isPreviousBarChartDateEnabled.collectAsStateWithLifecycle()
    val pieExpenses by viewModel.pieChartExpenses.collectAsStateWithLifecycle()
    val pieDate by viewModel.pieChartDate.collectAsStateWithLifecycle()
    val monthlyShownInPie by viewModel.monthlyShownInPieChart.collectAsStateWithLifecycle()
    val isNextPieDateEnabled by viewModel.isNextPieChartDateEnabled.collectAsStateWithLifecycle()
    val isPreviousPieDateEnabled by viewModel.isPreviousPieChartDateEnabled.collectAsStateWithLifecycle()
    val isPreviousBalanceYearEnabled by viewModel.isPreviousBalanceYearEnabled.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .testTag("dashboard_scroll")
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BudgetIndicatorCard(
            monthShown = monthShown,
            thisMonthSum = thisMonthSum,
            thisYearSum = thisYearSum,
            monthlyBudget = monthlyBudget,
            onToggleMonthShown = viewModel::toggleMonthShown
        )
        ExpensesCard(
            todaySum = todaySum,
            monthShown = monthShown,
            thisMonthSum = thisMonthSum,
            thisYearSum = thisYearSum,
            today = viewModel.today
        )
        MonthlyExpensesChartCard(
            barChartData = barChartData,
            monthlyBudget = monthlyBudget,
            onPreviousDate = { viewModel.previousBarChartDate() },
            onNextDate = { viewModel.nextBarChartDate() },
            onToday = { viewModel.todayBarChartDate() },
            selectedRange = barChartRange,
            onSelectedRangeChange = viewModel::selectBarChartRange,
            shownCount = barChartShownCount,
            onVisibleCountChanged = viewModel::setBarChartVisibleItems,
            trackingStart = barChartTrackingStart,
            isNextDateEnabled = isNextBarDateEnabled,
            isPreviousDateEnabled = isPreviousBarDateEnabled
        )
        AnnualBalanceCard(
            balanceYear = annualBalanceData.year,
            incomesSum = annualBalanceData.incomes,
            expensesSum = annualBalanceData.expenses,
            onPreviousYear = { viewModel.previousBalanceYear() },
            onNextYear = { viewModel.nextBalanceYear() },
            onToday = { viewModel.todayBalanceYear() },
            isPreviousYearEnabled = isPreviousBalanceYearEnabled,
            isNextYearEnabled = annualBalanceData.year < viewModel.today.year
        )
        ExpensesByCategoryCard(
            expenses = pieExpenses,
            date = pieDate,
            monthlyShown = monthlyShownInPie,
            onSwitchData = { viewModel.switchPieChartData(it) },
            onPreviousDate = { viewModel.previousPieChartDate() },
            onNextDate = { viewModel.nextPieChartDate() },
            onToday = { viewModel.todayPieChartDate() },
            isNextDateEnabled = isNextPieDateEnabled,
            isPreviousDateEnabled = isPreviousPieDateEnabled
        )
        Spacer(modifier = Modifier.height(112.dp)) // Floating Action Button space
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MyFinanceTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            DashboardContent(
                today = LocalDate.of(2024, 5, 29),
                monthShown = true,
                thisMonthSum = 450.0,
                thisYearSum = 5400.0,
                monthlyBudget = 1000.0,
                todaySum = 25.0,
                balanceYear = 2024,
                incomesSum = 2000.0,
                expensesSum = 1500.0,
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
                pieExpenses = emptyList(),
                pieDate = LocalDate.of(2024, 5, 29),
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
internal fun DashboardContent(
    today: LocalDate,
    monthShown: Boolean,
    thisMonthSum: Double,
    thisYearSum: Double,
    monthlyBudget: Double,
    todaySum: Double,
    balanceYear: Int,
    incomesSum: Double,
    expensesSum: Double,
    barChartData: List<MonthlyTotal>,
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
    barChartRange: Int = 0,
    onSelectBarChartRange: (Int) -> Unit = {},
    barChartShownCount: Int = barChartData.size,
    onVisibleCountChangedBarChart: (Int) -> Unit = {},
    barChartTrackingStart: LocalDate? = null,
    isNextBarDateEnabled: Boolean = true,
    isPreviousBarDateEnabled: Boolean = true,
    onSwitchPieData: (Boolean) -> Unit,
    onPreviousPieDate: () -> Unit,
    onNextPieDate: () -> Unit,
    onTodayPieData: () -> Unit,
    isNextPieDateEnabled: Boolean = true,
    isPreviousPieDateEnabled: Boolean = true,
    isPreviousBalanceYearEnabled: Boolean = true
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
            thisYearSum = thisYearSum,
            today = today
        )
        MonthlyExpensesChartCard(
            barChartData = barChartData,
            monthlyBudget = monthlyBudget,
            onPreviousDate = onPreviousBarDate,
            onNextDate = onNextBarDate,
            onToday = onTodayBarDate,
            selectedRange = barChartRange,
            onSelectedRangeChange = onSelectBarChartRange,
            shownCount = barChartShownCount,
            onVisibleCountChanged = onVisibleCountChangedBarChart,
            trackingStart = barChartTrackingStart,
            isNextDateEnabled = isNextBarDateEnabled,
            isPreviousDateEnabled = isPreviousBarDateEnabled
        )
        AnnualBalanceCard(
            balanceYear = balanceYear,
            incomesSum = incomesSum,
            expensesSum = expensesSum,
            onPreviousYear = onPreviousYear,
            onNextYear = onNextYear,
            onToday = onTodayAnnualBalance,
            isPreviousYearEnabled = isPreviousBalanceYearEnabled,
            isNextYearEnabled = balanceYear < today.year
        )
        ExpensesByCategoryCard(
            expenses = pieExpenses,
            date = pieDate,
            monthlyShown = monthlyShownInPie,
            onSwitchData = onSwitchPieData,
            onPreviousDate = onPreviousPieDate,
            onNextDate = onNextPieDate,
            onToday = onTodayPieData,
            isNextDateEnabled = isNextPieDateEnabled,
            isPreviousDateEnabled = isPreviousPieDateEnabled
        )
    }
}
