package com.frafio.myfinance.ui.features.home.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.components.EditTransactionSheet
import com.frafio.myfinance.ui.components.EmptyView
import com.frafio.myfinance.ui.components.EmptyListItem
import com.frafio.myfinance.ui.components.SearchBar
import com.frafio.myfinance.ui.components.TotalItem
import com.frafio.myfinance.ui.components.TransactionListItem
import com.frafio.myfinance.ui.home.expenses.ExpensesViewModel
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.doubleToPrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    onSelectDateRange: () -> Unit,
    onItemLongClick: (Expense, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
    modifier: Modifier = Modifier,
) {
    val expenses by viewModel.expenses.collectAsState()
    val totalFilteredExpenses by viewModel.totalFilteredExpenses.collectAsState()
    val isExpensesEmpty by viewModel.isExpensesEmpty.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedLabels by viewModel.selectedLabels.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    val itemMetadata by viewModel.itemMetadata.collectAsState()
    val labels by viewModel.labels.collectAsState()

    var showFilterSheet by remember { mutableStateOf(value = false) }
    var showCategorySheet by remember { mutableStateOf(value = false) }
    var categoryTargetExpense by remember { mutableStateOf<Expense?>(value = null) }
    var showEditSheet by remember { mutableStateOf(value = false) }
    var editTargetExpense by remember { mutableStateOf<Expense?>(value = null) }
    var showLabelsSheet by remember { mutableStateOf(value = false) }
    var labelsTargetExpense by remember { mutableStateOf<Expense?>(value = null) }
    var showNewLabelInLabels by remember { mutableStateOf(value = true) }

    FilterExpensesSheet(
        show = showFilterSheet,
        onDismiss = {
            if (showFilterSheet) {
                showFilterSheet = false
            }
        },
        categoryEnabled = selectedCategories.size != 9,
        labelEnabled = labels.isNotEmpty(),
        dateRangeEnabled = dateRange == null,
        onSelectCategory = {
            categoryTargetExpense = null
            showCategorySheet = true
        },
        onSelectLabel = {
            labelsTargetExpense = null
            showNewLabelInLabels = false
            showLabelsSheet = true
        },
        onSelectDateRange = onSelectDateRange,
    )

    CategorySheet(
        show = showCategorySheet,
        onDismiss = {
            if (showCategorySheet) {
                showCategorySheet = false
            }
        },
        expense = categoryTargetExpense,
        disabledCategories = if (categoryTargetExpense == null) selectedCategories else emptyList(),
        onCategorySelected = { categoryId ->
            if (categoryTargetExpense == null) {
                viewModel.onCategoryFilterChanged(categoryId)
            } else {
                viewModel.updateCategory(categoryTargetExpense!!, categoryId)
            }
            if (showCategorySheet) {
                showCategorySheet = false
            }
        },
    )

    EditTransactionSheet(
        show = showEditSheet,
        transaction = editTargetExpense ?: Expense(),
        onDismiss = {
            if (showEditSheet) {
                showEditSheet = false
            }
        },
        onLabels = {
            labelsTargetExpense = editTargetExpense
            showNewLabelInLabels = true
            showLabelsSheet = true
            if (showEditSheet) {
                showEditSheet = false
            }
        },
        onEdit = {
            editTargetExpense?.let {
                onItemLongClick(it, expenses.indexOf(it))
            }
            if (showEditSheet) {
                showEditSheet = false
            }
        },
        onDelete = {
            editTargetExpense?.let { viewModel.deleteExpense(it) }
            if (showEditSheet) {
                showEditSheet = false
            }
        },
    )

    LabelsSheet(
        show = showLabelsSheet,
        onDismiss = {
            if (showLabelsSheet) {
                showLabelsSheet = false
            }
        },
        expense = labelsTargetExpense,
        labels = labels,
        selectedLabels = selectedLabels,
        showEditLabel = showNewLabelInLabels,
        onNewLabel = viewModel::addLabel,
        onLabelCheckedChanged = { label, checked ->
            if (labelsTargetExpense == null) {
                viewModel.onLabelFilterChanged(label, checked)
            } else {
                if (checked) {
                    viewModel.addLabelToExpense(labelsTargetExpense!!, label)
                } else {
                    viewModel.removeLabelFromExpense(labelsTargetExpense!!, label)
                }
            }
        },
        onDeleteLabel = viewModel::deleteLabel,
        onEditLabel = viewModel::editLabel,
    )

    ExpensesContent(
        expenses = expenses,
        totalFilteredExpenses = totalFilteredExpenses,
        isExpensesEmpty = isExpensesEmpty,
        searchQuery = searchQuery,
        selectedCategories = selectedCategories,
        selectedLabels = selectedLabels,
        dateRange = dateRange,
        scrollToIdFlow = viewModel.scrollToId,
        itemMetadata = itemMetadata,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onCategoryFilterChanged = viewModel::onCategoryFilterChanged,
        onLabelFilterChanged = viewModel::onLabelFilterChanged,
        onDateFilterChanged = { viewModel.onDateFilterChanged(it) },
        onLoadMore = { viewModel.loadMore() },
        onFilterClick = { showFilterSheet = true },
        onItemLongClick = { expense, _ ->
            editTargetExpense = expense
            showEditSheet = true
        },
        onCategoryClick = { expense, _ ->
            categoryTargetExpense = expense
            showCategorySheet = true
        },
        getDateLabel = getDateLabel,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpensesContent(
    expenses: List<Expense>,
    totalFilteredExpenses: Double,
    isExpensesEmpty: Boolean?,
    searchQuery: String,
    selectedCategories: List<Int>,
    selectedLabels: List<String>,
    dateRange: Pair<LocalDate, LocalDate>?,
    scrollToIdFlow: Flow<String?>,
    itemMetadata: Map<Int, Pair<Int, Int>>,
    onSearchQueryChanged: (String) -> Unit,
    onCategoryFilterChanged: (Int) -> Unit,
    onLabelFilterChanged: (String, Boolean) -> Unit,
    onDateFilterChanged: (Pair<LocalDate, LocalDate>?) -> Unit,
    onLoadMore: () -> Unit,
    onFilterClick: () -> Unit,
    onItemLongClick: (Expense, Int) -> Unit,
    onCategoryClick: (Expense, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
    modifier: Modifier = Modifier,
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

            AnimatedVisibility(
                visible = (selectedCategories.isNotEmpty()) || (selectedLabels.isNotEmpty()) || (dateRange != null),
                enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
            ) {
                Column {
                    var checked by remember { mutableStateOf(value = false) }
                    val colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChipBar(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f),
                            categories = selectedCategories,
                            labels = selectedLabels,
                            dateFilter = dateRange,
                            getDateLabel = getDateLabel,
                            onCategoryRemoved = onCategoryFilterChanged,
                            onLabelRemoved = { onLabelFilterChanged(it, false) },
                        ) {
                            onDateFilterChanged(null)
                        }
                        FilledTonalIconToggleButton(
                            checked = checked,
                            onCheckedChange = { checked = it },
                            shapes = IconButtonDefaults.toggleableShapes()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_query_stats_filled),
                                contentDescription = null
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = checked,
                        enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                        exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                    ) {
                        SegmentedListItem(
                            onClick = { },
                            shapes = ListItemDefaults.shapes(
                                shape = ListItemDefaults.shapes().selectedShape
                            ),
                            colors = colors,
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(MaterialShapes.Pill.toShape())
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_bar_chart_4_bars),
                                        contentDescription = null,
                                    )
                                }
                            },
                            content = {
                                Text(
                                    text = stringResource(id = R.string.total_expenses),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Normal
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(id = R.string.on_filters),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Normal,
                                )
                            },
                            trailingContent = {
                                Text(
                                    modifier = Modifier.padding(end = 8.dp),
                                    text = doubleToPrice(totalFilteredExpenses),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
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

@Composable
fun ExpensesList(
    expenses: List<Expense>,
    onItemLongClick: (Expense, Int) -> Unit,
    onCategoryClick: (Expense, Int) -> Unit,
    onLoadMore: () -> Unit,
    scrollToIdFlow: Flow<String?>,
    itemMetadata: Map<Int, Pair<Int, Int>>,
) {
    val listState = rememberLazyListState()
    val currentExpenses by rememberUpdatedState(newValue = expenses)
    var isFirstScroll by remember { mutableStateOf(value = true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { index ->
                val threshold = currentExpenses.size - 5
                (index != null) && (index >= threshold)
            }
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
            .fillMaxSize(),
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
                    ) {
                        onCategoryClick(expense, index)
                    }
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
            id = "total_28_10_2023",
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
            totalFilteredExpenses = 150.0,
            isExpensesEmpty = false,
            searchQuery = "",
            selectedCategories = emptyList(),
            selectedLabels = emptyList(),
            dateRange = null,
            scrollToIdFlow = flowOf(null),
            onSearchQueryChanged = {},
            onCategoryFilterChanged = {},
            onLabelFilterChanged = { _, _ -> },
            onDateFilterChanged = {},
            onLoadMore = {},
            onFilterClick = {},
            onItemLongClick = { _, _ -> },
            onCategoryClick = { _, _ -> },
            getDateLabel = { _, _ -> "Oct 27, 2023" },
            itemMetadata = emptyMap(),
        )
    }
}
