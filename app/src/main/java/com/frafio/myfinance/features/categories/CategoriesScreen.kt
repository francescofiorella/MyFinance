package com.frafio.myfinance.features.categories

import androidx.compose.animation.core.snap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.charts.ShareBar
import com.frafio.charts.ShareBarDefaults
import com.frafio.charts.ShareBarItem
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppTopBar
import com.frafio.myfinance.core.components.CategoryShareItem
import com.frafio.myfinance.core.components.PeriodStepper
import com.frafio.myfinance.core.components.SpendingChangeChip
import com.frafio.myfinance.core.data.model.CategorySum
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.monthToString
import java.time.LocalDate

@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBackClick: () -> Unit,
    onCategoryClick: (categoryId: Int, month: LocalDate) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CategoriesContent(
        state = state,
        onBackClick = onBackClick,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onCurrentMonth = viewModel::goToCurrentMonth,
        onCategoryClick = { onCategoryClick(it, state.month) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesContent(
    state: CategoriesUiState,
    onBackClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit,
    onCategoryClick: (categoryId: Int) -> Unit
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
                title = { Text(text = stringResource(id = R.string.categories), maxLines = 1) },
                onNavigationClick = onBackClick
            )

            val (spent, unspent) = state.spending.partition { it.total > 0.0 }
            // Keyed by position, not category: rows stay in place and their bar animates to the new category's share.
            LazyColumn(
                modifier = Modifier
                    .testTag("categories_scroll")
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item(key = "month_header") {
                    MonthHeader(
                        state = state,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onCurrentMonth = onCurrentMonth
                    )
                }

                itemsIndexed(spent, key = { index, _ -> "row_$index" }) { index, spending ->
                    CategorySpendingItem(
                        spending = spending,
                        index = index,
                        count = spent.size,
                        onClick = { onCategoryClick(spending.category.id) }
                    )
                }

                if (unspent.isNotEmpty()) {
                    item(key = "no_spending_label") {
                        Text(
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                            text = stringResource(id = R.string.no_spending_this_month),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Same keys as above, so a row crossing into this group keeps its state and fades its bar out.
                itemsIndexed(unspent, key = { index, _ -> "row_${spent.size + index}" }) { index, spending ->
                    CategorySpendingItem(
                        spending = spending,
                        index = index,
                        count = unspent.size,
                        onClick = { onCategoryClick(spending.category.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    state: CategoriesUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onCurrentMonth: () -> Unit
) {
    Column(modifier = Modifier.padding(start = 32.dp, end = 24.dp, bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = monthToString(state.month),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = doubleToPrice(state.total),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            PeriodStepper(
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                onToday = onCurrentMonth,
                previousDescription = stringResource(R.string.previous_month),
                nextDescription = stringResource(R.string.next_month),
                todayDescription = stringResource(R.string.current_month),
                isPreviousEnabled = state.isPreviousEnabled,
                isNextEnabled = state.isNextEnabled
            )
        }
        val totals = state.spending.associate { it.category.id to it.total }
        // No animation while the first month loads, like the rows; only later month changes animate.
        var hasLoaded by remember { mutableStateOf(false) }
        LaunchedEffect(state.spending.isNotEmpty()) {
            if (state.spending.isNotEmpty()) hasLoaded = true
        }
        val isDark = isSystemInDarkTheme()
        ShareBar(
            modifier = Modifier.padding(top = 12.dp, end = 8.dp),
            // Fixed category order, so each segment grows or shrinks in place when the month changes.
            items = ExpenseCategories.all.map { ShareBarItem(value = totals[it.id] ?: 0.0, color = it.colors.container(isDark)) },
            animationSpec = if (hasLoaded) ShareBarDefaults.AnimationSpec else snap()
        )
    }
}

@Composable
private fun CategorySpendingItem(
    modifier: Modifier = Modifier,
    spending: CategorySpending,
    index: Int,
    count: Int,
    onClick: () -> Unit
) {
    CategoryShareItem(
        category = spending.category,
        total = spending.total,
        share = spending.share,
        index = index,
        count = count,
        onClick = onClick,
        modifier = modifier.testTag("category_item"),
        trailing = {
            if (spending.total > 0.0) {
                SpendingChangeChip(change = spending.change)
            }
        }
    )
}

/** Fixed sample data for previews and screenshots. */
fun sampleCategoriesUiState(month: LocalDate): CategoriesUiState = CategoriesUiState(
    month = month.withDayOfMonth(1),
    spending = buildCategorySpending(
        current = listOf(
            CategorySum(0, 450.0), CategorySum(1, 210.0), CategorySum(5, 320.0),
            CategorySum(7, 95.0), CategorySum(6, 40.0), CategorySum(8, 25.0)
        ),
        previous = listOf(
            CategorySum(0, 465.0), CategorySum(1, 228.0), CategorySum(5, 285.0),
            CategorySum(7, 95.0), CategorySum(3, 60.0)
        )
    ),
    isPreviousEnabled = true,
    isNextEnabled = false
)

@Preview(showBackground = true)
@Composable
fun CategoriesPreview() {
    MyFinanceTheme {
        CategoriesContent(
            state = sampleCategoriesUiState(LocalDate.of(2024, 5, 1)),
            onBackClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onCurrentMonth = {},
            onCategoryClick = {}
        )
    }
}
