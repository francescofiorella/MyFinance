package com.frafio.myfinance.ui.features.home.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.components.EmptyView
import com.frafio.myfinance.ui.components.EmptyListItem
import com.frafio.myfinance.ui.components.SearchBar
import com.frafio.myfinance.ui.components.TotalItem
import com.frafio.myfinance.ui.components.TransactionListItem
import com.frafio.myfinance.ui.home.expenses.ExpensesViewModel
import com.frafio.myfinance.ui.theme.MyFinanceTheme
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
    val itemMetadata by viewModel.itemMetadata.collectAsState()

    ExpensesContent(
        expenses = expenses,
        isExpensesEmpty = isExpensesEmpty,
        searchQuery = searchQuery,
        selectedCategories = selectedCategories,
        dateRange = dateRange,
        scrollToIdFlow = viewModel.scrollToId,
        itemMetadata = itemMetadata,
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
    itemMetadata: Map<Int, Pair<Int, Int>>,
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
                scrollToIdFlow = scrollToIdFlow,
                itemMetadata = itemMetadata
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
    scrollToIdFlow: Flow<String?>,
    itemMetadata: Map<Int, Pair<Int, Int>>
) {
    val listState = rememberLazyListState()
    val currentExpenses by rememberUpdatedState(expenses)
    var isFirstScroll by remember { mutableStateOf(true) }

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
                    .filter { it.isNotEmpty() }
                    .first()
                    .let { list ->
                        val index = list.indexOfFirst { it.id.startsWith(id) }
                        val finalIndex = if (index != -1) index else 0
                        if (isFirstScroll) {
                            listState.scrollToItem(finalIndex)
                            isFirstScroll = false
                        } else {
                            listState.animateScrollToItem(finalIndex)
                        }
                    }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                    TotalItem(transaction = expense)
                }

                FirestoreEnums.CATEGORIES.JOLLY.value -> {
                    EmptyListItem(messageRes = R.string.no_expenses)
                }

                else -> {
                    val metadata = itemMetadata[index] ?: Pair(0, 1)
                    TransactionListItem(
                        transaction = expense,
                        indexInGroup = metadata.first,
                        countInGroup = metadata.second,
                        onClick = { },
                        onLongClick = { onItemLongClick(expense, index) },
                        onIconClick = { onCategoryClick(expense, index) }
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(88.dp)) // Floating Action Button space
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
            getDateLabel = { _, _ -> "Oct 27, 2023" },
            itemMetadata = emptyMap()
        )
    }
}
