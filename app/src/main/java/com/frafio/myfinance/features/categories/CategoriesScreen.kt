package com.frafio.myfinance.features.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.CategoryIcon
import com.frafio.myfinance.core.components.PeriodStepper
import com.frafio.myfinance.core.components.SpendingChangeChip
import com.frafio.myfinance.core.data.model.CategorySum
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPercent
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
            CategoriesTopBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .testTag("categories_scroll")
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
            ) {
                MonthHeader(
                    state = state,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onCurrentMonth = onCurrentMonth
                )

                val (spent, unspent) = state.spending.partition { it.total > 0.0 }
                spent.forEachIndexed { index, spending ->
                    CategorySpendingItem(
                        spending = spending,
                        index = index,
                        count = spent.size,
                        onClick = { onCategoryClick(spending.category.id) }
                    )
                }

                if (unspent.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                        text = stringResource(id = R.string.no_spending_this_month),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    unspent.forEachIndexed { index, spending ->
                        CategorySpendingItem(
                            spending = spending,
                            index = index,
                            count = unspent.size,
                            onClick = { onCategoryClick(spending.category.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CategoriesTopBar(onBackClick: () -> Unit) {
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
            text = stringResource(id = R.string.categories),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
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
        ShareBar(
            modifier = Modifier.padding(top = 12.dp, end = 8.dp),
            spending = state.spending
        )
    }
}

/** One bar split into a segment per category, sized by its share of the month. */
@Composable
private fun ShareBar(spending: List<CategorySpending>, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val shares = spending.filter { it.share > 0.0 }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        shares.forEach {
            Box(
                modifier = Modifier
                    .weight(it.share.toFloat())
                    .fillMaxHeight()
                    .background(it.category.colors.container(isDark))
            )
        }
    }
}

@Composable
private fun CategorySpendingItem(
    spending: CategorySpending,
    index: Int,
    count: Int,
    onClick: () -> Unit
) {
    val hasSpending = spending.total > 0.0
    SegmentedListItem(
        onClick = onClick,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes()
        ),
        content = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(category = spending.category, colored = hasSpending)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = stringResource(id = spending.category.nameRes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (hasSpending) {
                                "${doubleToPrice(spending.total)} · ${doubleToPercent(spending.share)}"
                            } else {
                                doubleToPrice(0.0)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (hasSpending) {
                        SpendingChangeChip(change = spending.change)
                    }
                }

                if (hasSpending) {
                    ProportionBar(
                        modifier = Modifier.padding(start = 52.dp, top = 8.dp),
                        fraction = spending.share,
                        category = spending.category
                    )
                }
            }
        },
        modifier = Modifier
            .testTag("category_item")
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = if (index == count - 1) 0.dp else 2.dp)
    )
}

/** A thin track filled up to [fraction] (0 to 1) in the category's color. */
@Composable
private fun ProportionBar(fraction: Double, category: ExpenseCategory, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.toFloat().coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(category.colors.container(isDark))
        )
    }
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
