package com.frafio.myfinance.features.home.budget

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.components.EditTransactionSheet
import com.frafio.myfinance.core.components.EmptyListItem
import com.frafio.myfinance.core.components.TotalItem
import com.frafio.myfinance.core.components.TransactionListItem
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.doubleToPrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onEditIncome: (Income, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val incomes by viewModel.incomes.collectAsStateWithLifecycle()
    val isIncomesEmpty by viewModel.isIncomesEmpty.collectAsStateWithLifecycle()
    val itemMetadata by viewModel.itemMetadata.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val annualBudget by viewModel.annualBudget.collectAsStateWithLifecycle()

    var showEditBudgetSheet by remember { mutableStateOf(value = false) }
    var showEditIncomeSheet by remember { mutableStateOf(value = false) }
    var editTargetIncome by remember { mutableStateOf<Income?>(value = null) }

    EditBudgetSheet(
        show = showEditBudgetSheet,
        budget = monthlyBudget,
        onDismiss = {
            if (showEditBudgetSheet) {
                showEditBudgetSheet = false
            }
        },
        onEditBudget = { viewModel.setMonthlyBudget(it) },
    )

    EditTransactionSheet(
        show = showEditIncomeSheet,
        transaction = editTargetIncome ?: Income(),
        onDismiss = {
            if (showEditIncomeSheet) {
                showEditIncomeSheet = false
            }
        },
        onLabels = {},
        onEdit = {
            editTargetIncome?.let {
                onEditIncome(it, incomes.indexOf(it))
            }
            if (showEditIncomeSheet) {
                showEditIncomeSheet = false
            }
        },
        onDelete = {
            editTargetIncome?.let { viewModel.deleteIncome(it) }
            if (showEditIncomeSheet) {
                showEditIncomeSheet = false
            }
        },
    )

    BudgetContent(
        incomes = incomes,
        isIncomesEmpty = isIncomesEmpty,
        itemMetadata = itemMetadata,
        monthlyBudget = monthlyBudget,
        annualBudget = annualBudget,
        scrollToIdFlow = viewModel.scrollToId,
        onLoadMore = viewModel::loadMore,
        onItemLongClick = { income, _ ->
            editTargetIncome = income
            showEditIncomeSheet = true
        },
        onEditBudgetClick = { showEditBudgetSheet = true },
        onDeleteBudget = viewModel::deleteMonthlyBudget,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BudgetContent(
    incomes: List<Income>,
    isIncomesEmpty: Boolean,
    monthlyBudget: Double,
    annualBudget: Double,
    onEditBudgetClick: () -> Unit,
    onDeleteBudget: () -> Unit,
    itemMetadata: Map<Int, Pair<Int, Int>>,
    onItemLongClick: (Income, Int) -> Unit,
    onLoadMore: () -> Unit,
    scrollToIdFlow: Flow<String?>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val currentIncomes by rememberUpdatedState(newValue = incomes)
    var isFirstScroll by remember { mutableStateOf(value = true) }

    val headerCount = 2 // Budget Card + Header Text

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { index ->
                val threshold = (currentIncomes.size + headerCount) - 5
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
                snapshotFlow { currentIncomes }
                    .first { list -> list.any { it.id.startsWith(id) } }
                    .let { list ->
                        val index = list.indexOfFirst { it.id.startsWith(id) }
                        if (index != -1) {
                            val finalIndex = index + headerCount
                            if (isFirstScroll) {
                                listState.scrollToItem(finalIndex)
                                isFirstScroll = false
                            } else {
                                listState.animateScrollToItem(finalIndex)
                            }
                        }
                    }
            } else {
                if (isFirstScroll) {
                    listState.scrollToItem(0)
                    isFirstScroll = false
                } else {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        item {
            BudgetOverview(
                monthlyBudget = monthlyBudget,
                annualBudget = annualBudget,
                onEditBudgetClick = onEditBudgetClick,
                onDeleteBudget = onDeleteBudget
            )
        }

        item {
            SectionHeader(title = stringResource(R.string.incomes))
        }

        if (isIncomesEmpty) {
            item {
                IncomeEmptyState(modifier = Modifier.fillParentMaxSize())
            }
        } else {
            itemsIndexed(
                items = incomes,
                key = { _, income -> income.id },
                contentType = { _, income ->
                    when (income.category) {
                        FirestoreEnums.CATEGORIES.TOTAL.value -> "total"
                        FirestoreEnums.CATEGORIES.JOLLY.value -> "jolly"
                        else -> "income"
                    }
                }
            ) { index, income ->
                val metadata = itemMetadata[index] ?: Pair(0, 1)
                IncomeItem(
                    income = income,
                    indexInGroup = metadata.first,
                    countInGroup = metadata.second,
                    onLongClick = { onItemLongClick(income, index) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(88.dp)) // Floating Action Button space
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BudgetOverview(
    monthlyBudget: Double,
    annualBudget: Double,
    onEditBudgetClick: () -> Unit,
    onDeleteBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 64.dp)
    ) {
        Text(
            text = stringResource(R.string.currency) + " "
                    + doubleToPrice(monthlyBudget).replace(
                stringResource(R.string.currency),
                ""
            ).trim(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.annual_budget) + " ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = doubleToPrice(annualBudget),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val editInteractionSource = remember { MutableInteractionSource() }
        val deleteInteractionSource = remember { MutableInteractionSource() }
        ButtonGroup(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            overflowIndicator = { menuState ->
                ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
            }
        ) {
            customItem(
                {
                    FilledIconButton(
                        modifier = Modifier
                            .size(IconButtonDefaults.smallContainerSize())
                            .animateWidth(editInteractionSource),
                        onClick = onEditBudgetClick,
                        shapes = IconButtonDefaults.shapes(
                            shape = IconButtonDefaults.smallSquareShape,
                        ),
                        interactionSource = editInteractionSource
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit_filled),
                            contentDescription = null,
                        )
                    }
                },
                {}
            )
            customItem(
                {
                    FilledIconButton(
                        modifier = Modifier
                            .size(IconButtonDefaults.smallContainerSize())
                            .animateWidth(deleteInteractionSource),
                        onClick = onDeleteBudget,
                        enabled = monthlyBudget != 0.0,
                        shapes = IconButtonDefaults.shapes(
                            shape = IconButtonDefaults.smallSquareShape,
                        ),
                        interactionSource = deleteInteractionSource
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete_filled),
                            contentDescription = null
                        )
                    }
                },
                {}
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 64.dp),
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun IncomeEmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(top = 64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.warning_budget),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IncomeItem(
    income: Income,
    indexInGroup: Int,
    countInGroup: Int,
    onLongClick: () -> Unit,
) {
    when (income.category) {
        FirestoreEnums.CATEGORIES.TOTAL.value -> {
            TotalItem(transaction = income)
        }

        FirestoreEnums.CATEGORIES.JOLLY.value -> {
            EmptyListItem(messageRes = R.string.no_incomes)
        }

        else -> {
            TransactionListItem(
                transaction = income,
                indexInGroup = indexInGroup,
                countInGroup = countInGroup,
                onClick = { },
                onLongClick = onLongClick
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BudgetPreview() {
    MyFinanceTheme {
        BudgetContent(
            incomes = listOf(
                Income(
                    id = "2024",
                    year = 2024,
                    month = 0,
                    category = FirestoreEnums.CATEGORIES.TOTAL.value,
                    price = 12000.0,
                ),
                Income(
                    id = "1",
                    name = "Salary",
                    price = 1000.0,
                    day = 1,
                    month = 1,
                    year = 2024,
                    category = 0,
                ),
                Income(
                    id = "2",
                    name = "Freelance",
                    price = 500.0,
                    day = 15,
                    month = 1,
                    year = 2024,
                    category = 0,
                )
            ),
            isIncomesEmpty = false,
            itemMetadata = mapOf(
                1 to Pair(0, 2),
                2 to Pair(1, 2)
            ),
            monthlyBudget = 1200.0,
            annualBudget = 14400.0,
            scrollToIdFlow = emptyFlow(),
            onLoadMore = {},
            onItemLongClick = { _, _ -> },
            onEditBudgetClick = {},
            onDeleteBudget = {},
        )
    }
}
