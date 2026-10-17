package com.frafio.myfinance.features.categorydetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.BarChart
import com.frafio.myfinance.core.components.CategoryIcon
import com.frafio.myfinance.core.components.SpendingChangeChip
import com.frafio.myfinance.core.components.TransactionListItem
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPercent
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.monthToString
import java.time.LocalDate

@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel,
    onBackClick: () -> Unit,
    onSeeAllExpenses: () -> Unit
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    CategoryDetailContent(
        category = viewModel.category,
        month = viewModel.month,
        stats = stats,
        onBackClick = onBackClick,
        onSeeAllExpenses = {
            viewModel.onSeeAllExpenses()
            onSeeAllExpenses()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailContent(
    category: ExpenseCategory,
    month: LocalDate,
    stats: CategoryDetailStats?,
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
            CategoryDetailTopBar(
                title = stringResource(id = category.nameRes),
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .testTag("category_detail_scroll")
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
            ) {
                Header(category = category, month = month)

                if (stats != null) {
                    StatsGrid(stats = stats, year = month.year)
                    TrendCard(stats = stats)

                    SectionTitle(text = stringResource(id = R.string.biggest_expenses))
                    if (stats.topExpenses.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(horizontal = 32.dp),
                            text = stringResource(id = R.string.no_spending_this_month),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        stats.topExpenses.forEachIndexed { index, expense ->
                            TransactionListItem(
                                transaction = expense,
                                indexInGroup = index,
                                countInGroup = stats.topExpenses.size,
                                onClick = {},
                                onLongClick = {},
                                supportingText = expense.getDateString()
                            )
                        }
                    }

                    if (stats.frequent.isNotEmpty()) {
                        SectionTitle(text = stringResource(id = R.string.most_frequent))
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

                    if (stats.labels.isNotEmpty()) {
                        SectionTitle(text = stringResource(id = R.string.by_label))
                        stats.labels.forEachIndexed { index, label ->
                            DetailRow(
                                title = label.label,
                                subtitle = null,
                                value = doubleToPrice(label.total),
                                index = index,
                                count = stats.labels.size
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    FilledTonalButton(
                        modifier = Modifier
                            .testTag("category_see_all")
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        onClick = onSeeAllExpenses,
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text(text = stringResource(id = R.string.see_all_expenses))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CategoryDetailTopBar(title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        FilledTonalIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBackClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                contentDescription = stringResource(id = R.string.navigate_up),
            )
        }

        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Header(category: ExpenseCategory, month: LocalDate) {
    Column(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CategoryIcon(
            category = category,
            size = IconButtonDefaults.largeContainerSize().height,
            iconSize = IconButtonDefaults.largeIconSize,
            shape = MaterialShapes.Cookie12Sided.toShape()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = monthToString(month),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(id = category.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatsGrid(stats: CategoryDetailStats, year: Int) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.category_month_spending),
                value = doubleToPrice(stats.monthTotal),
                extra = {
                    SpendingChangeChip(
                        change = stats.change,
                        textStyle = MaterialTheme.typography.labelSmall
                    )
                }
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.monthly_average),
                value = doubleToPrice(stats.average),
                extra = {
                    Text(
                        text = stringResource(id = R.string.category_month_spending_description),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.year_to_date),
                value = doubleToPrice(stats.yearToDate),
                extra = {
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(id = R.string.share_of_month),
                value = doubleToPercent(stats.share),
                extra = {
                    Text(
                        text = stringResource(id = R.string.share_of_month_description),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    extra: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            extra()
        }
    }
}

@Composable
private fun TrendCard(stats: CategoryDetailStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        shape = ListItemDefaults.shapes().selectedShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = stringResource(id = R.string.spending_trend),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BarChart(
                entries = stats.trend
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DetailRow(
    title: String,
    subtitle: String?,
    value: String,
    index: Int,
    count: Int
) {
    SegmentedListItem(
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes()
        ),
        content = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = if (index == count - 1) 0.dp else 2.dp)
    )
}

/** Fixed sample data for previews and screenshots: Dining over the year up to [month]. */
fun sampleCategoryDetailStats(month: LocalDate): CategoryDetailStats {
    val dining = ExpenseCategories.get(5)
    val monthlyTotals =
        listOf(180.0, 240.0, 210.0, 260.0, 300.0, 230.0, 270.0, 250.0, 310.0, 285.0, 0.0, 320.0)
    val expenses = monthlyTotals.flatMapIndexed { index, total ->
        val date = month.minusMonths((monthlyTotals.size - 1 - index).toLong()).withDayOfMonth(10)
        if (total == 0.0) {
            emptyList()
        } else {
            listOf(
                sampleExpense(
                    "Sushi Bar",
                    total * 0.4,
                    date,
                    dining.id,
                    listOf("Dinner"),
                    "s$index"
                ),
                sampleExpense(
                    "Pizzeria Da Mario",
                    total * 0.35,
                    date.plusDays(5),
                    dining.id,
                    listOf("Dinner", "Friends"),
                    "p$index"
                ),
                sampleExpense(
                    "Coffee",
                    total * 0.25,
                    date.plusDays(9),
                    dining.id,
                    emptyList(),
                    "c$index"
                ),
            )
        }
    }.sortedWith(compareByDescending<Expense> { it.year }.thenByDescending { it.month }
        .thenByDescending { it.day })
    return buildCategoryDetail(month, expenses, monthTotalAll = 1140.0)
}

private fun sampleExpense(
    name: String,
    price: Double,
    date: LocalDate,
    category: Int,
    labels: List<String>,
    id: String
) =
    Expense(
        name = name,
        price = price,
        year = date.year,
        month = date.monthValue,
        day = date.dayOfMonth,
        category = category,
        labels = labels,
        id = id
    )

@Preview(showBackground = true)
@Composable
fun CategoryDetailPreview() {
    MyFinanceTheme {
        val month = LocalDate.of(2024, 5, 1)
        CategoryDetailContent(
            category = ExpenseCategories.get(5),
            month = month,
            stats = sampleCategoryDetailStats(month),
            onBackClick = {},
            onSeeAllExpenses = {}
        )
    }
}
