package com.frafio.myfinance.features.managecategories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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

            // One row open at a time; tapping it again closes it.
            var expandedId by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                modifier = Modifier
                    .testTag("manage_categories_list")
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
                    val isExpanded = expandedId == category.id
                    CategoryExpandableItem(
                        category = category,
                        isExpanded = isExpanded,
                        onToggleExpand = { expandedId = if (isExpanded) null else category.id },
                        index = index,
                        count = categories.size
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryExpandableItem(
    category: ExpenseCategory,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    index: Int,
    count: Int
) {
    val expandState = stringResource(id = if (isExpanded) R.string.expanded else R.string.collapsed)
    SegmentedListItem(
        onClick = onToggleExpand,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = count,
            defaultShapes = ListItemDefaults.shapes()
        ),
        content = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryIcon(category = category)

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        text = stringResource(id = category.nameRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isExpanded) R.drawable.ic_keyboard_arrow_up_filled else R.drawable.ic_keyboard_arrow_down_filled
                            ),
                            contentDescription = null,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeIn(MaterialTheme.motionScheme.fastSpatialSpec()),
                    exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeOut(MaterialTheme.motionScheme.fastSpatialSpec())
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
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
            .semantics { stateDescription = expandState }
    )
}

@Preview(showBackground = true)
@Composable
fun ManageCategoriesPreview() {
    MyFinanceTheme {
        ManageCategoriesContent(categories = ExpenseCategories.all, onBackClick = {})
    }
}
