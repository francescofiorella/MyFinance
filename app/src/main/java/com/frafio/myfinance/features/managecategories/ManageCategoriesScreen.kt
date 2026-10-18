package com.frafio.myfinance.features.managecategories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.AppTopBar
import com.frafio.myfinance.core.components.CategoryIcon
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun ManageCategoriesScreen(onBackClick: () -> Unit) {
    ManageCategoriesContent(categories = ExpenseCategories.all, onBackClick = onBackClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesContent(
    categories: List<ExpenseCategory>,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppTopBar(
                title = { Text(text = stringResource(id = R.string.categories), maxLines = 1) },
                onNavigationClick = onBackClick
            )

            LazyColumn(
                modifier = Modifier
                    .testTag("manage_categories_list")
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
                    CategoryItem(category = category, index = index, count = categories.size)
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(category: ExpenseCategory, index: Int, count: Int) {
    SegmentedListItem(
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes()
        ),
        content = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(category = category)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = stringResource(id = category.nameRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(id = category.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = Modifier
            .testTag("manage_category_item")
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = if (index == count - 1) 0.dp else 2.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun ManageCategoriesPreview() {
    MyFinanceTheme {
        ManageCategoriesContent(categories = ExpenseCategories.all, onBackClick = {})
    }
}
