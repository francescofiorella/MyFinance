package com.frafio.myfinance.ui.home.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.Transaction
import com.frafio.myfinance.ui.composable.components.SheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun EditTransactionSheet(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    SheetDialog(
        icon = getCategoryIcon(transaction.category ?: 0),
        title = transaction.name ?: "",
        label = transaction.getDateString(),
        labelFirst = false,
        endContent = transaction.getPriceString(),
        modifier = modifier
    ) {
        Column {
            // Edit item
            Surface(
                onClick = {
                    onEdit()
                    onDismiss()
                },
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_outline),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 15.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = stringResource(id = R.string.edit),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Delete item
            Surface(
                onClick = {
                    onDelete()
                    onDismiss()
                },
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete_outline),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 15.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = stringResource(id = R.string.delete),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(categoryId: Int): Int {
    return when (categoryId) {
        FirestoreEnums.CATEGORIES.HOUSING.value -> R.drawable.ic_home_filled
        FirestoreEnums.CATEGORIES.GROCERIES.value -> R.drawable.ic_shopping_cart_filled
        FirestoreEnums.CATEGORIES.PERSONAL_CARE.value -> R.drawable.ic_self_care_filled
        FirestoreEnums.CATEGORIES.ENTERTAINMENT.value -> R.drawable.ic_theater_comedy_filled
        FirestoreEnums.CATEGORIES.EDUCATION.value -> R.drawable.ic_school_filled
        FirestoreEnums.CATEGORIES.DINING.value -> R.drawable.ic_restaurant_filled
        FirestoreEnums.CATEGORIES.HEALTH.value -> R.drawable.ic_vaccines_filled
        FirestoreEnums.CATEGORIES.TRANSPORTATION.value -> R.drawable.ic_directions_subway_filled
        FirestoreEnums.CATEGORIES.MISCELLANEOUS.value -> R.drawable.ic_grid_3x3_filled
        else -> R.drawable.ic_grid_3x3_filled
    }
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
