package com.frafio.myfinance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.Transaction
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.getCategoryIcon
import com.frafio.myfinance.utils.getCategoryName

@Composable
fun TotalItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (transaction.category == FirestoreEnums.CATEGORIES.TOTAL.value && transaction.month == 0) {
                transaction.year.toString()
            } else {
                transaction.getDateString(extended = true)
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if ((transaction.price ?: 0.0) >= 0) {
            Text(
                text = transaction.getPriceString(true),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransactionListItem(
    transaction: Transaction,
    indexInGroup: Int,
    countInGroup: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    onIconClick: (() -> Unit) = { }
) {
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
        leadingContent = {
            if (transaction is Expense) {
                FilledTonalIconButton(
                    onClick = onIconClick,
                    shapes = IconButtonDefaults.shapes(
                        shape = IconButtonDefaults.smallRoundShape,
                        pressedShape = IconButtonDefaults.smallSquareShape
                    )
                ) {
                    Icon(
                        painter = painterResource(getCategoryIcon(transaction.category)),
                        contentDescription = null,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
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
        },
        content = {
            Text(
                text = transaction.name ?: "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                maxLines = 1
            )
        },
        supportingContent = {
            val supportingText = if (transaction is Income) {
                transaction.getDateString(extended = false)
            } else {
                stringResource(getCategoryName(transaction.category))
            }
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        },
        trailingContent = {
            Text(
                text = transaction.getPriceString(true),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun JollyListItem(
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
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTotalItem() {
    MyFinanceTheme {
        TotalItem(
            transaction = Income(
                year = 2024,
                month = 0,
                category = FirestoreEnums.CATEGORIES.TOTAL.value,
                price = 1200.0
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExpenseListItem() {
    MyFinanceTheme {
        TransactionListItem(
            transaction = Expense(
                name = "Pizza",
                price = 15.0,
                category = 1,
                day = 1,
                month = 1,
                year = 2024
            ),
            indexInGroup = 0,
            countInGroup = 1,
            onClick = {},
            onLongClick = {},
            onIconClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIncomeListItem() {
    MyFinanceTheme {
        TransactionListItem(
            transaction = Income(
                name = "Salary",
                price = 2500.0,
                category = 0,
                day = 1,
                month = 1,
                year = 2024
            ),
            indexInGroup = 0,
            countInGroup = 1,
            onClick = {},
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewJollyListItem() {
    MyFinanceTheme {
        JollyListItem(messageRes = R.string.no_expenses)
    }
}
