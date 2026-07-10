package com.frafio.myfinance.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCategoryIcon
import com.frafio.myfinance.core.utils.getCategoryName

@Composable
fun TotalItem(
    modifier: Modifier = Modifier,
    transaction: Transaction
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (transaction is Income) {
                transaction.year.toString()
            } else {
                transaction.getDateString(extended = true)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if ((transaction.price ?: 0.0) >= 0) {
            Text(
                text = transaction.getPriceString(true),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionListItem(
    modifier: Modifier = Modifier,
    transaction: Transaction,
    indexInGroup: Int,
    countInGroup: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onIconClick: (() -> Unit) = { }
) {
    var labelsToDisplay by remember(transaction.id) { mutableStateOf(transaction.labels) }
    if (transaction.labels.isNotEmpty()) {
        labelsToDisplay = transaction.labels
    }

    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
    SegmentedListItem(
        onClick = onClick,
        onLongClick = onLongClick,
        shapes = if (indexInGroup == 0 && countInGroup == 1) {
            ListItemDefaults.shapes(
                shape = ListItemDefaults.shapes().selectedShape
            )
        } else {
            ListItemDefaults.segmentedShapes(
                index = indexInGroup,
                count = countInGroup,
                defaultShapes = ListItemDefaults.shapes()
            )
        },
        colors = colors,
        content = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Leading Content
                if (transaction is Expense) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        FilledTonalIconButton(
                            modifier = Modifier
                                .padding(end = 12.dp),
                            onClick = onIconClick,
                            shapes = IconButtonDefaults.shapes(
                                shape = IconButtonDefaults.smallRoundShape,
                            )
                        ) {
                            Icon(
                                painter = painterResource(getCategoryIcon(transaction.category)),
                                contentDescription = null,
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val firstLetter =
                            transaction.name?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
                        Text(
                            text = firstLetter,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.name ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    val supportingText = if (transaction is Income) {
                        transaction.getDateString(extended = false)
                    } else {
                        stringResource(getCategoryName(transaction.category))
                    }
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    modifier = Modifier
                        .padding(start = 12.dp),
                    text = transaction.getPriceString(true),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        supportingContent = {
            val showLabels = transaction is Expense && transaction.labels.isNotEmpty()
            AnimatedVisibility(
                visible = showLabels,
                enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
                exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec())
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp, start = 52.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    labelsToDisplay.forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(AssistChipDefaults.shape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(
                bottom = if (indexInGroup == countInGroup - 1)
                    0.dp else 2.dp
            )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyListItem(
    messageRes: Int,
    modifier: Modifier = Modifier
) {
    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
    SegmentedListItem(
        onClick = {},
        onLongClick = {},
        shapes = ListItemDefaults.shapes(
            shape = ListItemDefaults.shapes().selectedShape
        ),
        colors = colors,
        content = {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseListItems() {
    MyFinanceTheme {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            TotalItem(
                transaction = Expense(
                    day = 2,
                    month = 1,
                    year = 2024,
                    category = FirestoreEnums.CATEGORIES.TOTAL.value,
                    price = 0.0
                )
            )
            EmptyListItem(messageRes = R.string.no_expenses)
            TotalItem(
                transaction = Expense(
                    day = 1,
                    month = 1,
                    year = 2024,
                    category = FirestoreEnums.CATEGORIES.TOTAL.value,
                    price = 15.0
                )
            )
            TransactionListItem(
                transaction = Expense(
                    name = "Pizza",
                    price = 13.0,
                    category = FirestoreEnums.CATEGORIES.DINING.value,
                    day = 1,
                    month = 1,
                    year = 2024,
                    labels = listOf("Dinner", "Cheat Meal")
                ),
                indexInGroup = 0,
                countInGroup = 2,
                onClick = {},
                onLongClick = {},
                onIconClick = {}
            )
            TransactionListItem(
                transaction = Expense(
                    name = "Cola",
                    price = 2.0,
                    category = FirestoreEnums.CATEGORIES.DINING.value,
                    day = 1,
                    month = 1,
                    year = 2024,
                    labels = emptyList()
                ),
                indexInGroup = 1,
                countInGroup = 2,
                onClick = {},
                onLongClick = {},
                onIconClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIncomeListItems() {
    MyFinanceTheme {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            TotalItem(
                transaction = Income(
                    year = 2024,
                    month = 1,
                    day = 1,
                    category = FirestoreEnums.CATEGORIES.TOTAL.value,
                    price = 2500.0
                )
            )
            TransactionListItem(
                transaction = Income(
                    name = "Salary",
                    price = 2500.0,
                    category = FirestoreEnums.CATEGORIES.INCOME.value,
                    day = 1,
                    month = 1,
                    year = 2024,
                    labels = emptyList()
                ),
                indexInGroup = 0,
                countInGroup = 1,
                onClick = {},
                onLongClick = {}
            )
        }
    }
}
