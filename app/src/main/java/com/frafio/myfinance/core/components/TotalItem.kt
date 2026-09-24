package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.core.components.preview.PreviewTransactions
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.core.theme.MyFinanceTheme

/** The header of a day of expenses or a year of incomes, with its total. */
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
        if (transaction.price >= 0) {
            Text(
                text = transaction.getPriceString(true),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TotalItemPreview() {
    MyFinanceTheme {
        Column {
            TotalItem(transaction = PreviewTransactions.expenseTotal(price = 0.0, day = 2))
            TotalItem(transaction = PreviewTransactions.expenseTotal(price = 15.0, day = 1))
            TotalItem(transaction = PreviewTransactions.incomeTotal)
        }
    }
}
