package com.frafio.myfinance.ui.features.home.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.components.EmptyView
import com.frafio.myfinance.ui.components.SearchBar
import com.frafio.myfinance.ui.home.expenses.ExpensesViewModel
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.getCategoryIcon
import com.frafio.myfinance.utils.getCategoryName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    onFilterClick: () -> Unit,
    onItemLongClick: (Expense, Int) -> Unit,
    onCategoryClick: (Expense, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expenses.collectAsState()
    val isExpensesEmpty by viewModel.isExpensesEmpty.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()

    ExpensesContent(
        expenses = expenses,
        isExpensesEmpty = isExpensesEmpty,
        searchQuery = searchQuery,
        selectedCategories = selectedCategories,
        dateRange = dateRange,
        scrollToIdFlow = viewModel.scrollToId,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onCategoryFilterChanged = viewModel::onCategoryFilterChanged,
        onDateFilterChanged = { viewModel.onDateFilterChanged(it) },
        onLoadMore = { viewModel.loadMore() },
        onFilterClick = onFilterClick,
        onItemLongClick = onItemLongClick,
        onCategoryClick = onCategoryClick,
        getDateLabel = getDateLabel,
        modifier = modifier
    )
}

@Composable
fun ExpensesContent(
    expenses: List<Expense>,
    isExpensesEmpty: Boolean?,
    searchQuery: String,
    selectedCategories: List<Int>,
    dateRange: Pair<LocalDate, LocalDate>?,
    scrollToIdFlow: Flow<String?>,
    onSearchQueryChanged: (String) -> Unit,
    onCategoryFilterChanged: (Int) -> Unit,
    onDateFilterChanged: (Pair<LocalDate, LocalDate>?) -> Unit,
    onLoadMore: () -> Unit,
    onFilterClick: () -> Unit,
    onItemLongClick: (Expense, Int) -> Unit,
    onCategoryClick: (Expense, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (isExpensesEmpty == true) {
            EmptyView(
                imageResLight = R.drawable.image_audit_pana,
                imageResDark = R.drawable.image_shared_goals_amico,
                messageRes = R.string.warning_home
            )
        } else {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChanged,
                onFilterClick = onFilterClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (selectedCategories.isNotEmpty() || dateRange != null) {
                FilterChipBar(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    categories = selectedCategories,
                    dateFilter = dateRange,
                    getDateLabel = getDateLabel,
                    onCategoryRemoved = onCategoryFilterChanged,
                    onDateRemoved = { onDateFilterChanged(null) }
                )
            }

            ExpensesList(
                expenses = expenses,
                onItemLongClick = onItemLongClick,
                onCategoryClick = onCategoryClick,
                onLoadMore = onLoadMore,
                scrollToIdFlow = scrollToIdFlow
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpensesList(
    expenses: List<Expense>,
    onItemLongClick: (Expense, Int) -> Unit,
    onCategoryClick: (Expense, Int) -> Unit,
    onLoadMore: () -> Unit,
    scrollToIdFlow: Flow<String?>
) {
    val listState = rememberLazyListState()
    val currentExpenses by rememberUpdatedState(expenses)

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { it != null && it >= currentExpenses.size - 5 }
            .distinctUntilChanged()
            .collect {
                onLoadMore()
            }
    }

    LaunchedEffect(scrollToIdFlow) {
        scrollToIdFlow.collect { id ->
            if (id != null) {
                snapshotFlow { currentExpenses }
                    .filter { it.any { expense -> expense.id.startsWith(id) } }
                    .first()
                    .let { list ->
                        val index = list.indexOfFirst { it.id.startsWith(id) }
                        if (index != -1) {
                            listState.animateScrollToItem(index)
                        }
                    }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 1.dp)
    ) {
        var i = 0
        var tot = 0
        itemsIndexed(
            items = expenses,
            key = { _, expense -> expense.id },
            contentType = { _, expense ->
                when (expense.category) {
                    FirestoreEnums.CATEGORIES.TOTAL.value -> "total"
                    FirestoreEnums.CATEGORIES.JOLLY.value -> "jolly"
                    else -> "expense"
                }
            }
        ) { index, expense ->
            when (expense.category) {
                FirestoreEnums.CATEGORIES.TOTAL.value -> {
                    TotalItem(expense, index)
                }

                FirestoreEnums.CATEGORIES.JOLLY.value -> {
                    SegmentedListItem(
                        onClick = {},
                        onLongClick = {},
                        shapes = ListItemDefaults.shapes().copy(
                            shape = ListItemDefaults.shapes().selectedShape
                        ),
                        colors = ListItemDefaults.colors().copy(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        content = {
                            Text(
                                text = stringResource(R.string.no_expenses),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 1.dp),
                    )
                }

                else -> {
                    i = 0
                    tot = 1
                    for (t in index + 1..<expenses.size) {
                        if (expenses[t].getLocalDate().isEqual(expense.getLocalDate())) {
                            tot++
                        } else {
                            break
                        }
                    }
                    for (t in index - 1 downTo 0) {
                        if (expenses[t].category != FirestoreEnums.CATEGORIES.TOTAL.value && expenses[t].getLocalDate()
                                .isEqual(expense.getLocalDate())
                        ) {
                            tot++
                            i++
                        } else {
                            break
                        }
                    }
                    SegmentedListItem(
                        onClick = {},
                        onLongClick = { onItemLongClick(expense, index) },
                        shapes = if (i == 0 && tot == 1) {
                            ListItemDefaults.shapes().copy(
                                shape = ListItemDefaults.shapes().selectedShape
                            )
                        } else {
                            ListItemDefaults.segmentedShapes(
                                index = i,
                                count = tot,
                                defaultShapes = ListItemDefaults.shapes()
                            )
                        },
                        colors = ListItemDefaults.colors().copy(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        leadingContent = {
                            IconButton(
                                onClick = { onCategoryClick(expense, index) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(),
                            ) {
                                Icon(
                                    painter = painterResource(getCategoryIcon(expense.category)),
                                    contentDescription = null,
                                )
                            }
                        },
                        content = {
                            Text(
                                text = expense.name ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(getCategoryName(expense.category)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        },
                        trailingContent = {
                            Text(
                                text = expense.getPriceString(true),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 1.dp),
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Floating Action Button space
        }
    }
}

@Composable
fun TotalItem(
    expense: Expense,
    index: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = if (index == 0) {
                    8.dp
                } else {
                    16.dp
                },
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = expense.getDateString(extended = true),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if ((expense.price ?: 0.0) >= 0) {
            Text(
                text = expense.getPriceString(true),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesContentPreview() {
    val sampleExpenses = listOf(
        Expense(
            name = FirestoreEnums.NAMES.TOTAL.value,
            price = 0.0,
            year = 2023,
            month = 10,
            day = 28,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            id = "total_28_10_2023"
        ),
        Expense(
            name = "Jolly",
            price = 0.0,
            year = 2023,
            month = 10,
            day = 28,
            category = FirestoreEnums.CATEGORIES.JOLLY.value,
            id = "0"
        ),
        Expense(
            name = FirestoreEnums.NAMES.TOTAL.value,
            price = 45.0,
            year = 2023,
            month = 10,
            day = 27,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            id = "total_27_10_2023"
        ),
        Expense(
            name = "Pizza Margherita",
            price = 8.5,
            year = 2023,
            month = 10,
            day = 27,
            category = FirestoreEnums.CATEGORIES.DINING.value,
            id = "1"
        ),
        Expense(
            name = "Groceries",
            price = 36.5,
            year = 2023,
            month = 10,
            day = 27,
            category = FirestoreEnums.CATEGORIES.GROCERIES.value,
            id = "2"
        ),
        Expense(
            name = FirestoreEnums.NAMES.TOTAL.value,
            price = 15.0,
            year = 2023,
            month = 10,
            day = 26,
            category = FirestoreEnums.CATEGORIES.TOTAL.value,
            id = "total_26_10_2023"
        ),
        Expense(
            name = "Bus Ticket",
            price = 1.5,
            year = 2023,
            month = 10,
            day = 26,
            category = FirestoreEnums.CATEGORIES.TRANSPORTATION.value,
            id = "3"
        )
    )
    MyFinanceTheme {
        ExpensesContent(
            expenses = sampleExpenses,
            isExpensesEmpty = false,
            searchQuery = "",
            selectedCategories = emptyList(),
            dateRange = null,
            scrollToIdFlow = flowOf(null),
            onSearchQueryChanged = {},
            onCategoryFilterChanged = {},
            onDateFilterChanged = {},
            onLoadMore = {},
            onFilterClick = {},
            onItemLongClick = { _, _ -> },
            onCategoryClick = { _, _ -> },
            getDateLabel = { _, _ -> "Oct 27, 2023" }
        )
    }
}
