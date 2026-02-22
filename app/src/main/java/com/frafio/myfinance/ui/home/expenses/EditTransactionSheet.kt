package com.frafio.myfinance.ui.home.expenses

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.MenuItem
import com.frafio.myfinance.data.model.Transaction
import com.frafio.myfinance.ui.composable.components.ListSheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.getCategoryIcon

@Composable
fun EditTransactionSheet(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListSheetDialog(
        icon = getCategoryIcon(transaction.category ?: 0),
        title = transaction.name ?: "",
        label = transaction.getDateString(),
        labelFirst = false,
        endContent = transaction.getPriceString(),
        onDismiss = onDismiss,
        items = listOf(
            MenuItem(
                iconRes = R.drawable.ic_edit_outline,
                textRes = R.string.edit,
                onClick = onEdit
            ),
            MenuItem(
                iconRes = R.drawable.ic_delete_outline,
                textRes = R.string.delete,
                onClick = onDelete
            )
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun EditTransactionSheetPreview() {
    MyFinanceTheme {
        EditTransactionSheet(
            transaction = Expense(
                name = "Expense",
                price = 0.00,
                year = 1970,
                month = 1,
                day = 1
            ),
            onDismiss = {},
            onEdit = {},
            onDelete = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
