package com.frafio.myfinance.ui.features.home.expenses

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.MenuItem
import com.frafio.myfinance.ui.components.ListSheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun FilterExpensesSheet(
    onDismiss: () -> Unit,
    categoryEnabled: Boolean,
    dateRangeEnabled: Boolean,
    onSelectCategory: () -> Unit,
    onSelectDateRange: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListSheetDialog(
        icon = R.drawable.ic_filter_list_filled,
        title = stringResource(id = R.string.filter),
        label = stringResource(id = R.string.select),
        onDismiss = onDismiss,
        items = listOf(
            MenuItem(
                iconRes = R.drawable.ic_grid_3x3_filled,
                textRes = R.string.category,
                enabled = categoryEnabled,
                onClick = onSelectCategory
            ),
            MenuItem(
                iconRes = R.drawable.ic_today_outline,
                textRes = R.string.date_range,
                enabled = dateRangeEnabled,
                onClick = onSelectDateRange
            )
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun FilterExpensesSheetPreview() {
    MyFinanceTheme {
        FilterExpensesSheet(
            onDismiss = {},
            onSelectCategory = {},
            onSelectDateRange = {},
            categoryEnabled = false,
            dateRangeEnabled = true,
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
