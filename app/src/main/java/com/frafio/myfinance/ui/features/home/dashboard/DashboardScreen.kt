package com.frafio.myfinance.ui.features.home.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.components.BarChart
import com.frafio.myfinance.ui.components.PieChart
import com.frafio.myfinance.utils.doubleToPrice
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
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
        BudgetCard(
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BudgetCard(
    monthShown: Boolean,
    thisMonthSum: Double,
    thisYearSum: Double,
    monthlyBudget: Double,
    onToggleMonthShown: (Boolean) -> Unit
) {
    val title = if (monthlyBudget > 0.0)
        stringResource(R.string.expenses_budget)
    else
        stringResource(R.string.this_month)
    val amount = if (monthShown) thisMonthSum else thisYearSum
    val totalBudget = if (monthShown) monthlyBudget else monthlyBudget * 12

    var selectedIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(top = 32.dp, bottom = 64.dp),
        ) {
            Text(
                text = doubleToPrice(amount),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (monthlyBudget > 0.0) {
                val stroke = Stroke(
                    width =
                        with(LocalDensity.current) {
                            12.dp.toPx()
                        },
                    cap = StrokeCap.Round,
                )
                val amplitude = with(LocalDensity.current) {
                    0.2.dp.toPx()
                }
                val progress = (amount / totalBudget).toFloat()
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = "progressAnimation"
                )
                LinearWavyProgressIndicator(
                    color = if (progress <= 1f)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    stopSize = 8.dp,
                    amplitude = { amplitude },
                    waveSpeed = 0.dp,
                    stroke = stroke,
                    trackStroke = stroke,
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                )

                Row {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier,
                        text = doubleToPrice(totalBudget),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    ToggleButton(
                        modifier = Modifier.weight(1f),
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        checked = selectedIndex == 0,
                        onCheckedChange = {
                            selectedIndex = 0
                            onToggleMonthShown(true)
                        },
                    ) {
                        Text(
                            text = stringResource(id = R.string.monthly)
                        )
                    }
                    Spacer(modifier = Modifier.width(ButtonGroupDefaults.ConnectedSpaceBetween))
                    ToggleButton(
                        modifier = Modifier.weight(1f),
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        checked = selectedIndex == 1,
                        onCheckedChange = {
                            selectedIndex = 1
                            onToggleMonthShown(false)
                        },
                    ) {
                        Text(
                            text = stringResource(id = R.string.annual)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpensesCard(
    todaySum: Double,
    monthShown: Boolean,
    thisMonthSum: Double,
    thisYearSum: Double
) {
    val now = LocalDate.now()
    val day = now.format(DateTimeFormatter.ofPattern("dd"))
    val month = now.format(DateTimeFormatter.ofPattern("MMMM")).replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
    val year = now.year

    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    SegmentedListItem(
        onClick = { },
        shapes = ListItemDefaults.segmentedShapes(
            index = 0,
            count = 2,
            defaultShapes = ListItemDefaults.shapes()
        ),
        colors = colors,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialShapes.Pill.toShape())
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_today_filled),
                    contentDescription = null,
                )
            }
        },
        content = {
            Text(
                text = stringResource(R.string.expenses_today),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        },
        supportingContent = {
            Text(
                text = "$day $month $year",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        },
        trailingContent = {
            Text(
                modifier = Modifier
                    .padding(end = 8.dp),
                text = doubleToPrice(todaySum),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp)
    )
    val title = if (monthShown)
        stringResource(R.string.this_year_next)
    else
        stringResource(R.string.this_month)

    val subTitle = if (monthShown) year.toString() else "$month $year"

    val amount = if (monthShown) thisYearSum else thisMonthSum

    SegmentedListItem(
        onClick = { },
        shapes = ListItemDefaults.segmentedShapes(
            index = 1,
            count = 2,
            defaultShapes = ListItemDefaults.shapes()
        ),
        colors = colors,
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialShapes.Sunny.toShape())
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_month),
                    contentDescription = null,
                )
            }
        },
        content = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        },
        supportingContent = {
            Text(
                text = subTitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        },
        trailingContent = {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = doubleToPrice(amount),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnnualBalanceCard(
    balanceYear: Int,
    incomesSum: Double,
    expensesSum: Double,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onToday: () -> Unit
) {
    val balance = incomesSum - expensesSum

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.annual_balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = balanceYear.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                FilledTonalIconButton(
                    onClick = onPreviousYear,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                        contentDescription = null
                    )
                }
                FilledTonalIconButton(
                    onClick = onNextYear,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                FilledTonalButton(
                    onClick = onToday,
                    shapes = ButtonDefaults.shapes(
                        pressedShape = ButtonDefaults.squareShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_today_filled),
                        contentDescription = null
                    )
                }
            }

            Text(
                text = doubleToPrice(abs(balance)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (balance < 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.incomes),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = doubleToPrice(incomesSum),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.expenses),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = doubleToPrice(expensesSum),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonthlyExpensesChartCard(
    barChartData: List<BarChartEntry>,
    monthlyBudget: Double,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit
) {
    var resetBarChart by remember { mutableIntStateOf(0) }

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
                FilledTonalIconButton(
                    onClick = onPreviousDate,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                        contentDescription = null
                    )
                }
                FilledTonalIconButton(
                    onClick = onNextDate,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                FilledTonalButton(
                    onClick = {
                        onToday()
                        resetBarChart++
                    },
                    shapes = ButtonDefaults.shapes(
                        pressedShape = ButtonDefaults.squareShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_today_filled),
                        contentDescription = null
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpensesByCategoryCard(
    expenses: List<Expense>,
    date: LocalDate,
    monthlyShown: Boolean,
    onSwitchData: (Boolean) -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onToday: () -> Unit
) {
    val values = remember(expenses) {
        val vals = MutableList(9) { 0.0 }
        expenses.forEach { p ->
            if (p.category != null && p.category <= 8) {
                vals[p.category] += p.price ?: 0.0
            }
        }
        vals
    }

    val formatter = remember(monthlyShown) {
        DateTimeFormatter.ofPattern(if (monthlyShown) "MMMM uuuu" else "uuuu")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    text = stringResource(R.string.expenses_by_category),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalIconButton(
                    onClick = onPreviousDate,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                        contentDescription = null
                    )
                }
                FilledTonalIconButton(
                    onClick = onNextDate,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallSquareShape,
                        pressedShape = IconButtonDefaults.smallRoundShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_keyboard_arrow_right_filled),
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                FilledTonalButton(
                    onClick = onToday,
                    shapes = ButtonDefaults.shapes(
                        pressedShape = ButtonDefaults.squareShape
                    )
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_today_filled),
                        contentDescription = null
                    )
                }
            }

            Row(modifier = Modifier.width(IntrinsicSize.Max)) {
                TonalToggleButton(
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                    checked = monthlyShown,
                    onCheckedChange = {
                        onSwitchData(true)
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.monthly)
                    )
                }
                Spacer(modifier = Modifier.width(ButtonGroupDefaults.ConnectedSpaceBetween))
                TonalToggleButton(
                    modifier = Modifier.weight(1f),
                    shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                    checked = !monthlyShown,
                    onCheckedChange = {
                        onSwitchData(false)
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.annual)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PieChart(
                    data = values,
                    animate = true
                )
            }

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = date.format(formatter).replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        BudgetCard(
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
