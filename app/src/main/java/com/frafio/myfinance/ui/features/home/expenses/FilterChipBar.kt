package com.frafio.myfinance.ui.features.home.expenses

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FirestoreEnums
import com.frafio.myfinance.ui.theme.MyFinanceTheme
import com.frafio.myfinance.utils.getCategoryIcon
import com.frafio.myfinance.utils.getCategoryName
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChipBar(
    modifier: Modifier = Modifier,
    categories: List<Int>,
    dateFilter: Pair<LocalDate, LocalDate>?,
    getDateLabel: (LocalDate, LocalDate) -> String,
    onCategoryRemoved: (Int) -> Unit,
    onDateRemoved: () -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy((-8).dp)
    ) {
        dateFilter?.let {
            FilterInputChip(
                text = getDateLabel(it.first, it.second),
                icon = R.drawable.ic_today_filled,
                onDismiss = onDateRemoved
            )
        }
        categories.forEach { categoryId ->
            FilterInputChip(
                text = stringResource(id = getCategoryName(categoryId)),
                icon = getCategoryIcon(categoryId),
                onDismiss = { onCategoryRemoved(categoryId) }
            )
        }
    }
}

@Composable
private fun FilterInputChip(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes icon: Int?,
    onDismiss: () -> Unit
) {
    InputChip(
        modifier = modifier,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            icon?.let {
                Icon(
                    modifier = Modifier.size(InputChipDefaults.IconSize),
                    painter = painterResource(id = icon),
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingIcon = {
            Icon(
                modifier = Modifier
                    .clickable(
                        onClick = onDismiss
                    )
                    .size(InputChipDefaults.IconSize),
                painter = painterResource(id = R.drawable.ic_close_filled),
                contentDescription = stringResource(id = R.string.remove),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onClick = { },
        selected = false
    )
}

@Preview(showBackground = true)
@Composable
fun FilterChipBarPreview() {
    MyFinanceTheme {
        FilterChipBar(
            modifier = Modifier.padding(horizontal = 8.dp),
            categories = listOf(
                FirestoreEnums.CATEGORIES.HOUSING.value,
                FirestoreEnums.CATEGORIES.GROCERIES.value,
                FirestoreEnums.CATEGORIES.PERSONAL_CARE.value
            ),
            dateFilter = Pair(LocalDate.now().minusDays(7), LocalDate.now()),
            getDateLabel = { _, _ -> "Last 7 days" },
            onCategoryRemoved = {},
            onDateRemoved = {}
        )
    }
}