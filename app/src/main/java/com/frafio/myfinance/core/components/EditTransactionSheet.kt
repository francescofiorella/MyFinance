                                                                                      package com.frafio.myfinance.core.components

import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.preview.PreviewTransactions
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    show: Boolean,
    transaction: Transaction,
    onDismiss: () -> Unit,
    onLabels: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = mutableListOf(
        MenuItem.Resource(
            iconRes = R.drawable.ic_edit_outline,
            textRes = R.string.edit,
            testTag = "transaction_edit",
            onClick = onEdit
        ),
        MenuItem.Resource(
            iconRes = R.drawable.ic_content_copy_outline,
            textRes = R.string.duplicate,
            testTag = "transaction_duplicate",
            onClick = onDuplicate
        ),
        MenuItem.Resource(
            iconRes = R.drawable.ic_delete_outline,
            textRes = R.string.delete,
            testTag = "transaction_delete",
            onClick = onDelete
        )
    )
    if (transaction is Expense) {
        items.add(
            0,
            MenuItem.Resource(
                iconRes = R.drawable.ic_sell_outline,
                textRes = R.string.labels,
                testTag = "transaction_labels",
                onClick = onLabels
            )
        )
    }

    AppBottomSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        ListSheetDialog(
            icon = if (transaction is Expense) {
                getCategoryIcon(transaction.category)
            } else {
                null
            },
            title = transaction.name,
            label = transaction.getDateString(),
            labelFirst = false,
            endContent = transaction.getPriceString(),
            onDismiss = onDismiss,
            items = items,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditTransactionSheetPreview() {
    MyFinanceTheme {
        EditTransactionSheet(
            show = true,
            transaction = PreviewTransactions.placeholderExpense,
            onDismiss = {},
            onLabels = {},
            onEdit = {},
            onDuplicate = {},
            onDelete = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
