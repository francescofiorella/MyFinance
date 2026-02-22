package com.frafio.myfinance.ui.home.expenses

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.MenuItem
import com.frafio.myfinance.ui.composable.components.GridSheetDialog
import com.frafio.myfinance.ui.composable.components.ListSheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.getCategoryIcon

@Composable
fun CategorySheetDialog(
    modifier: Modifier = Modifier,
    expense: Expense? = null,
    disabledCategories: List<Int> = listOf(),
    onCategorySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        MenuItem(
            iconRes = R.drawable.ic_home_filled,
            textRes = R.string.housing,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.HOUSING.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.HOUSING.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_shopping_cart_filled,
            textRes = R.string.groceries,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.GROCERIES.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.GROCERIES.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_self_care_filled,
            textRes = R.string.personal_care,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.PERSONAL_CARE.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.PERSONAL_CARE.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_theater_comedy_filled,
            textRes = R.string.entertainment,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.ENTERTAINMENT.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.ENTERTAINMENT.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_school_filled,
            textRes = R.string.education,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.EDUCATION.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.EDUCATION.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_restaurant_filled,
            textRes = R.string.dining,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.DINING.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.DINING.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_vaccines_filled,
            textRes = R.string.health,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.HEALTH.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.HEALTH.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_directions_subway_filled,
            textRes = R.string.transportation,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.TRANSPORTATION.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.TRANSPORTATION.value) }
        ),
        MenuItem(
            iconRes = R.drawable.ic_grid_3x3_filled,
            textRes = R.string.miscellaneous,
            enabled = !disabledCategories.contains(FirestoreEnums.CATEGORIES.MISCELLANEOUS.value),
            onClick = { onCategorySelected(FirestoreEnums.CATEGORIES.MISCELLANEOUS.value) }
        )
    )

    val is600dp = booleanResource(id = R.bool.is600dp)

    @DrawableRes val icon = getCategoryIcon(expense?.category)
    val title = expense?.name ?: stringResource(id = R.string.category)
    val label = expense?.getDateString() ?: stringResource(id = R.string.select)
    val labelFirst = expense == null
    val endContent = expense?.getPriceString()
    if (is600dp) {
        ListSheetDialog(
            modifier = modifier,
            icon = icon,
            title = title,
            label = label,
            labelFirst = labelFirst,
            endContent = endContent,
            items = categories,
            onDismiss = onDismiss
        )
    } else {
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
fun CategorySheetDialogPreview() {
    MyFinanceTheme {
        CategorySheetDialog(
            disabledCategories = listOf(
                FirestoreEnums.CATEGORIES.ENTERTAINMENT.value,
                FirestoreEnums.CATEGORIES.MISCELLANEOUS.value
            ),
            onCategorySelected = {},
            onDismiss = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseCategorySheetDialogPreview() {
    MyFinanceTheme {
        CategorySheetDialog(
            expense = Expense(
                name = "Expense",
                price = 0.0,
                year = 1970,
                month = 1,
                day = 1,
                category = FirestoreEnums.CATEGORIES.HOUSING.value
            ),
            onCategorySelected = {},
            onDismiss = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
