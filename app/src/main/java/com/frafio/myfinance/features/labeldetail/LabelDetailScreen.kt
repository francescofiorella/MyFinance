package com.frafio.myfinance.features.labeldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.charts.BarChart
import com.frafio.charts.BarChartEntry
import com.frafio.charts.PieChart
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppTopBar
import com.frafio.myfinance.core.components.CategoryShareItem
import com.frafio.myfinance.core.components.DetailRow
import com.frafio.myfinance.core.components.EmptyListItem
import com.frafio.myfinance.core.components.SectionTitle
import com.frafio.myfinance.core.components.StatCard
import com.frafio.myfinance.core.components.TransactionListItem
import com.frafio.myfinance.core.components.categoryPieChartItem
import com.frafio.myfinance.core.components.toBarChartEntry
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.dateRangeToString
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LabelDetailScreen(
    viewModel: LabelDetailViewModel,
    onBackClick: () -> Unit,
    onSeeAllExpenses: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LabelDetailContent(
        label = viewModel.label,
        state = state,
        onBackClick = onBackClick,
        onSeeAllExpenses = {
            viewModel.onSeeAllExpenses()
            onSeeAllExpenses()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LabelDetailContent(
    label: String,
    state: LabelDetailUiState,
    onBackClick: () -> Unit,
    onSeeAllExpenses: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppTopBar(
                title = { Text(text = label, maxLines = 1) },
                onNavigationClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .testTag("label_detail_scroll")
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
            ) {
                when (state) {
                    LabelDetailUiState.Loading -> Unit
                    // The label lost its last expense while open.
                    LabelDetailUiState.Empty -> {
                        Header(dateRange = null)
                        EmptyListItem(messageRes = R.string.no_expenses)
                    }

                    is LabelDetailUiState.Loaded -> {
                        val stats = state.stats
                        Header(dateRange = dateRangeToString(stats.first, stats.last))
                        StatsGrid(stats = stats)
                        TimelineCard(timeline = stats.timeline)
                        CategoriesCard(categories = stats.categories)

                        if (stats.topExpenses.isNotEmpty()) {
                            SectionTitle(text = stringResource(id = R.string.biggest_expenses_overall))
                            stats.topExpenses.forEachIndexed { index, expense ->
                                TransactionListItem(
                                    // Only the other labels: this one is on every row.
                                    transaction = expense.copy(labels = expense.labels - label),
                                    indexInGroup = index,
                                    countInGroup = stats.topExpenses.size,
                                    supportingText = expense.getDateString()
                                )
                            }
                        }

                        if (stats.frequent.isNotEmpty()) {
                            SectionTitle(text = stringResource(id = R.string.frequent_expenses_title))
                            stats.frequent.forEachIndexed { index, frequent ->
                                DetailRow(
                                    title = frequent.name,
                                    subtitle = pluralStringResource(
                                        R.plurals.frequent_expense_times,
                                        frequent.count,
                                        frequent.count
                                    ),
                                    value = doubleToPrice(frequent.total),
                                    index = index,
                                    count = stats.frequent.size
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        FilledTonalButton(
                            modifier = Modifier
                                .testTag("label_see_all")
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = 16.dp),
                            onClick = onSeeAllExpenses,
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Text(text = stringResource(id = R.string.see_all_expenses))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Header(dateRange: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(IconButtonDefaults.largeContainerSize().height)
                .clip(MaterialShapes.Cookie12Sided.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sell_filled),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(IconButtonDefaults.largeIconSize)
            )
        }
        if (dateRange != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = dateRange,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatsGrid(stats: LabelDetailStats) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatRow(
            start = { modifier ->
                StatCard(modifier = modifier, label = stringResource(id = R.string.total), value = doubleToPrice(stats.total))
            },
            end = { modifier ->
                StatCard(modifier = modifier, label = stringResource(id = R.string.expenses), value = stats.count.toString())
            }
        )
        StatRow(
            start = { modifier ->
                StatCard(
                    modifier = modifier,
                    label = stringResource(id = R.string.active_days),
                    value = stats.activeDays.toString(),
                    extra = {
                        Text(
                            text = pluralStringResource(R.plurals.active_days_out_of, stats.spanDays, stats.spanDays),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            },
            end = { modifier ->
                StatCard(
                    modifier = modifier,
                    label = stringResource(id = R.string.average_per_active_day),
                    value = doubleToPrice(stats.averagePerActiveDay)
                )
            }
        )
        StatRow(
            start = { modifier ->
                StatCard(
                    modifier = modifier,
                    label = stringResource(id = R.string.average_per_expense),
                    value = doubleToPrice(stats.averagePerExpense)
                )
            },
            end = { modifier ->
                StatCard(
                    modifier = modifier,
                    label = stringResource(id = R.string.biggest_day),
                    value = doubleToPrice(stats.biggestDay.total),
                    extra = {
                        Text(
                            text = dateRangeToString(stats.biggestDay.date, stats.biggestDay.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        )
    }
}

@Composable
private fun TimelineCard(timeline: LabelTimeline) {
    // Index into the slots; -1 selects none, showing the average.
    var selectedSlot by rememberSaveable { mutableIntStateOf(-1) }
    val buckets = timeline.slots.count { it is TimelineSlot.Bucket }
    Card(
        modifier = Modifier
            .testTag("label_timeline")
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(id = R.string.spending_over_time),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BarChart(
                entries = timeline.slots.map { it.toBarChartEntry(timeline.unit) },
                selectedIndex = selectedSlot,
                onSelectedIndexChange = { selectedSlot = it },
                averageLabel = pluralStringResource(timeline.unit.averageRes, buckets, buckets),
                valueFormatter = ::doubleToPrice,
                barPadding = TIMELINE_BAR_PADDING
            )
        }
    }
}

/** The pie over every category, in their fixed order like the dashboard, then a row per category with spending. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoriesCard(categories: List<CategoryShare>) {
    // Index into ExpenseCategories.all; -1 selects none, showing the total.
    var selectedCategory by rememberSaveable { mutableIntStateOf(-1) }
    val totals = categories.associate { it.category.id to it.total }
    Card(
        modifier = Modifier
            .testTag("label_categories")
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(id = R.string.expenses_by_category),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                PieChart(
                    items = ExpenseCategories.all.map { category ->
                        categoryPieChartItem(
                            value = totals[category.id] ?: 0.0,
                            label = stringResource(category.nameRes),
                            icon = category.iconRes,
                            colors = category.colors
                        )
                    },
                    selectedIndex = selectedCategory,
                    onSelectedIndexChange = { selectedCategory = it },
                    totalLabel = stringResource(id = R.string.total),
                    valueFormatter = ::doubleToPriceWithoutDecimals,
                    selectedIconShape = MaterialShapes.VerySunny
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    categories.forEachIndexed { index, share ->
        val categoryIndex = ExpenseCategories.all.indexOf(share.category)
        val isSelected = selectedCategory == categoryIndex
        CategoryShareItem(
            category = share.category,
            total = share.total,
            share = share.share,
            index = index,
            count = categories.size,
            onClick = { selectedCategory = if (isSelected) -1 else categoryIndex },
            modifier = Modifier.testTag("label_category_item"),
            selected = isSelected
        )
    }
}

@Composable
private fun TimelineSlot.toBarChartEntry(unit: TimelineUnit): BarChartEntry = when (this) {
    is TimelineSlot.Bucket -> when (unit) {
        TimelineUnit.DAY -> BarChartEntry(
            key = unit.keyOf(start),
            value = total,
            label = start.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
            axisLabel = start.format(DateTimeFormatter.ofPattern("dd")),
            selectedAxisLabel = start.format(DateTimeFormatter.ofPattern("dd/MM"))
        )

        TimelineUnit.WEEK -> BarChartEntry(
            key = unit.keyOf(start),
            value = total,
            label = stringResource(R.string.timeline_week_of, start.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))),
            axisLabel = start.format(DateTimeFormatter.ofPattern("dd/MM")),
            selectedAxisLabel = start.format(DateTimeFormatter.ofPattern("dd/MM/yy"))
        )

        TimelineUnit.MONTH -> MonthlyTotal(total, start.year, start.monthValue).toBarChartEntry()

        TimelineUnit.YEAR -> BarChartEntry(
            key = unit.keyOf(start),
            value = total,
            label = start.year.toString(),
            axisLabel = start.year.toString()
        )
    }

    is TimelineSlot.Gap -> {
        val length = gapLength(this, unit)
        BarChartEntry(
            key = unit.keyOf(start),
            value = null,
            label = pluralStringResource(length.unit.descriptionRes, length.amount, length.amount),
            axisLabel = pluralStringResource(length.unit.shortRes, length.amount, length.amount),
            isBreak = true
        )
    }
}

private val TimelineUnit.averageRes: Int
    get() = when (this) {
        TimelineUnit.DAY -> R.plurals.trend_average_days
        TimelineUnit.WEEK -> R.plurals.trend_average_weeks
        TimelineUnit.MONTH -> R.plurals.trend_average_months
        TimelineUnit.YEAR -> R.plurals.trend_average_years
    }

private val GapUnit.descriptionRes: Int
    get() = when (this) {
        GapUnit.DAYS -> R.plurals.timeline_gap_days
        GapUnit.WEEKS -> R.plurals.timeline_gap_weeks
        GapUnit.MONTHS -> R.plurals.timeline_gap_months
        GapUnit.YEARS -> R.plurals.timeline_gap_years
    }

private val GapUnit.shortRes: Int
    get() = when (this) {
        GapUnit.DAYS -> R.plurals.timeline_gap_short_days
        GapUnit.WEEKS -> R.plurals.timeline_gap_short_weeks
        GapUnit.MONTHS -> R.plurals.timeline_gap_short_months
        GapUnit.YEARS -> R.plurals.timeline_gap_short_years
    }

// Thinner than the default, so a month of days keeps bars about as wide as the 24-month charts.
private val TIMELINE_BAR_PADDING = 1.5.dp

@Composable
private fun StatRow(
    start: @Composable (Modifier) -> Unit,
    end: @Composable (Modifier) -> Unit
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        start(Modifier.weight(1f))
        end(Modifier.weight(1f))
    }
}

/** Fixed sample data for previews and screenshots: a trip booked months ahead, newest first. */
fun sampleTripExpenses(): List<Expense> {
    val transport = CATEGORIES.TRANSPORTATION.value
    val dining = CATEGORIES.DINING.value
    val housing = CATEGORIES.HOUSING.value
    val entertainment = CATEGORIES.ENTERTAINMENT.value
    val misc = CATEGORIES.MISCELLANEOUS.value
    return listOf(
        sampleExpense("Airport train", 32.0, LocalDate.of(2025, 8, 19), transport),
        sampleExpense("Souvenirs", 85.0, LocalDate.of(2025, 8, 18), misc),
        sampleExpense("Ramen", 14.5, LocalDate.of(2025, 8, 18), dining),
        sampleExpense("Sushi dinner", 120.0, LocalDate.of(2025, 8, 17), dining),
        sampleExpense("TeamLab", 28.0, LocalDate.of(2025, 8, 16), entertainment),
        sampleExpense("Ramen", 12.0, LocalDate.of(2025, 8, 15), dining),
        sampleExpense("Shinkansen", 95.0, LocalDate.of(2025, 8, 14), transport),
        sampleExpense("Ramen", 13.0, LocalDate.of(2025, 8, 13), dining),
        sampleExpense("Airport train", 30.0, LocalDate.of(2025, 8, 12), transport),
        sampleExpense("JR Pass", 310.0, LocalDate.of(2025, 7, 20), transport),
        sampleExpense("Hotel Kyoto", 540.0, LocalDate.of(2025, 4, 10), housing),
        sampleExpense("Flight", 920.0, LocalDate.of(2025, 2, 3), transport)
    ).withLabel("Japan 2025")
}

/** Fixed sample data for previews and screenshots: a label used over more than a year, newest first. */
fun sampleLongRunningExpenses(): List<Expense> {
    val housing = CATEGORIES.HOUSING.value
    val misc = CATEGORIES.MISCELLANEOUS.value
    return listOf(
        sampleExpense("Paint", 140.0, LocalDate.of(2025, 4, 28), housing),
        sampleExpense("Plumber", 380.0, LocalDate.of(2025, 2, 11), housing),
        sampleExpense("Hardware store", 65.0, LocalDate.of(2025, 1, 18), misc),
        sampleExpense("Kitchen", 2400.0, LocalDate.of(2024, 11, 5), housing),
        sampleExpense("Hardware store", 48.0, LocalDate.of(2024, 9, 21), misc),
        sampleExpense("Electrician", 420.0, LocalDate.of(2024, 6, 3), housing),
        sampleExpense("Hardware store", 72.0, LocalDate.of(2024, 5, 12), misc),
        sampleExpense("Tiles", 1595.0, LocalDate.of(2024, 3, 2), housing)
    ).withLabel("Home renovation")
}

private fun sampleExpense(name: String, price: Double, date: LocalDate, category: Int) =
    Expense(
        name = name,
        price = price,
        year = date.year,
        month = date.monthValue,
        day = date.dayOfMonth,
        category = category,
        id = "$name-$date"
    )

private fun List<Expense>.withLabel(label: String) = map { it.copy(labels = listOf(label)) }

@Preview(showBackground = true)
@Composable
fun LabelDetailPreview() {
    MyFinanceTheme {
        LabelDetailContent(
            label = "Japan 2025",
            state = LabelDetailUiState.Loaded(buildLabelDetail(sampleTripExpenses())!!),
            onBackClick = {},
            onSeeAllExpenses = {}
        )
    }
}
