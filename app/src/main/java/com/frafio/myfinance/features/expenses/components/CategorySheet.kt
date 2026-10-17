package com.frafio.myfinance.features.expenses.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppBottomSheet
import com.frafio.myfinance.core.components.GridSheetDialog
import com.frafio.myfinance.core.components.MenuItem
import com.frafio.myfinance.core.components.preview.PreviewTransactions
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.theme.MyFinanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    expense: Expense? = null,
    disabledCategories: List<Int> = listOf(),
) {
    val categories = ExpenseCategories.all.map { category ->
        MenuItem.Resource(
            iconRes = category.iconRes,
            textRes = category.nameRes,
            enabled = !disabledCategories.contains(category.id),
            onClick = { onCategorySelected(category.id) }
        )
    }

    @DrawableRes val icon = ExpenseCategories.get(expense?.category).iconRes
    val title = expense?.name ?: stringResource(id = R.string.category)
    val label = expense?.getDateString() ?: stringResource(id = R.string.select)
    val labelFirst = expense == null
    val endContent = expense?.getPriceString()

    AppBottomSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        GridSheetDialog(
            modifier = modifier,
            icon = icon,
            title = title,
            label = label,
            labelFirst = labelFirst,
            endContent = endContent,
            rowSize = 3,
            items = categories,
            onDismiss = onDismiss
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategorySheetPreview() {
    MyFinanceTheme {
        CategorySheet(
            show = true,
            onDismiss = {},
            disabledCategories = listOf(
                FirestoreEnums.CATEGORIES.ENTERTAINMENT.value,
                FirestoreEnums.CATEGORIES.MISCELLANEOUS.value
            ),
            onCategorySelected = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseCategorySheetPreview() {
    MyFinanceTheme {
        CategorySheet(
            show = true,
            onDismiss = {},
            expense = PreviewTransactions.placeholderExpense.copy(
                category = FirestoreEnums.CATEGORIES.HOUSING.value
            ),
            onCategorySelected = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
