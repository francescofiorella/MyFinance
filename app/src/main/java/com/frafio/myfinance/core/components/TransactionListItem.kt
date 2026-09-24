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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.preview.PreviewTransactions
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCategoryIcon
import com.frafio.myfinance.core.utils.getCategoryName

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
    val expense = transaction as? Expense
    val supportingText = if (expense != null) {
        stringResource(getCategoryName(expense.category))
    } else {
        transaction.getDateString(extended = false)
    }

    SegmentedListItem(
        onClick = onClick,
        onLongClick = onLongClick,
        shapes = ListItemDefaults.segmentedShapes(
            index = indexInGroup,
            count = countInGroup,
            defaultShapes = ListItemDefaults.shapes()
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        content = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (expense != null) {
                    CategoryIconButton(category = expense.category, onClick = onIconClick)
                } else {
                    LetterAvatar(
                        text = transaction.name,
                        size = 40.dp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
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
            key(transaction.id) {
                LabelChips(labels = expense?.labels.orEmpty())
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

@Composable
private fun CategoryIconButton(
    category: Int,
    onClick: () -> Unit
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        FilledTonalIconButton(
            modifier = Modifier
                .padding(end = 12.dp),
            onClick = onClick,
            shapes = IconButtonDefaults.shapes(
                shape = IconButtonDefaults.smallRoundShape,
            )
        ) {
            Icon(
                painter = painterResource(getCategoryIcon(category)),
                contentDescription = stringResource(
                    R.string.change_category,
                    stringResource(getCategoryName(category))
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LabelChips(labels: List<String>) {
    // Removing the last label shrinks the row away; keep drawing the old chips until it is gone.
    var shownLabels by remember { mutableStateOf(labels) }
    if (labels.isNotEmpty()) {
        shownLabels = labels
    }

    AnimatedVisibility(
        visible = labels.isNotEmpty(),
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
            shownLabels.forEach { label ->
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
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseListItems() {
    MyFinanceTheme {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            TransactionListItem(
                transaction = PreviewTransactions.pizza,
                indexInGroup = 0,
                countInGroup = 2,
                onClick = {},
                onLongClick = {},
                onIconClick = {}
            )
            TransactionListItem(
                transaction = PreviewTransactions.cola,
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
            TransactionListItem(
                transaction = PreviewTransactions.salary,
                indexInGroup = 0,
                countInGroup = 1,
                onClick = {},
                onLongClick = {}
            )
        }
    }
}
