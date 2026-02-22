package com.frafio.myfinance.ui.composable.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.MenuItem
import com.frafio.myfinance.ui.theme.MyFinanceTheme

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
        CategoryGridSheetDialog(
            modifier = modifier,
            icon = icon,
            title = title,
            label = label,
            labelFirst = labelFirst,
            endContent = endContent,
            categories = categories,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun CategoryGridSheetDialog(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    label: String,
    labelFirst: Boolean = true,
    endContent: String? = null,
    categories: List<MenuItem>,
    onDismiss: () -> Unit,
) {
    SheetDialog(
        icon = icon,
        title = title,
        label = label,
        labelFirst = labelFirst,
        endContent = endContent,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(top = 5.dp)) {
            categories.chunked(3).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { category ->
                        CategoryGridItem(
                            modifier = Modifier.weight(1f),
                            icon = category.iconRes,
                            text = category.textRes,
                            enabled = category.enabled,
                            onClick = category.onClick,
                            onDismiss = onDismiss
                        )
                    }
                    // Fill remaining space if row is not full
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGridItem(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    @StringRes text: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = if (enabled) {
            modifier
                .fillMaxWidth()
        } else {
            modifier
                .fillMaxWidth()
                .alpha(0.38f)
        },
        onClick = {
            onClick()
            onDismiss()
        },
        enabled = enabled,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = modifier.padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = text),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getCategoryIcon(categoryId: Int?): Int {
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
