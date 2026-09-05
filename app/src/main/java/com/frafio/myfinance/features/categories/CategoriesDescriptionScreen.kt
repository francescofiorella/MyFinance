package com.frafio.myfinance.features.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesDescriptionScreen(
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
            CategoriesDescriptionTopBar(onBackClick = onBackClick)

            val categories = listOf(
                CategoryItem(R.string.housing, R.drawable.ic_home_filled, R.string.housing_description),
                CategoryItem(R.string.transportation, R.drawable.ic_directions_subway_filled, R.string.transportation_description),
                CategoryItem(R.string.groceries, R.drawable.ic_shopping_cart_filled, R.string.groceries_description),
                CategoryItem(R.string.health, R.drawable.ic_vaccines_filled, R.string.health_description),
                CategoryItem(R.string.personal_care, R.drawable.ic_face_filled, R.string.personal_care_description),
                CategoryItem(R.string.entertainment, R.drawable.ic_theater_comedy_filled, R.string.entertainment_description),
                CategoryItem(R.string.education, R.drawable.ic_school_filled, R.string.education_description),
                CategoryItem(R.string.miscellaneous, R.drawable.ic_grid_3x3_filled, R.string.miscellaneous_description)
            )

            var expandedIndex by remember { mutableStateOf<Int?>(null) }

            Column(
                modifier = Modifier
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .fillMaxHeight()
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEachIndexed { index, category ->
                    val isExpanded = expandedIndex == index
                    CategoryExpandableItem(
                        category = category,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedIndex = if (isExpanded) null else index
                        },
                        index = index,
                        totalCount = categories.size
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesDescriptionTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        FilledTonalIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBackClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_keyboard_arrow_left_filled),
                contentDescription = stringResource(id = R.string.back_arrow),
            )
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.categories),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryExpandableItem(
    category: CategoryItem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    index: Int,
    totalCount: Int
) {
    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )

    SegmentedListItem(
        onClick = onToggleExpand,
        colors = colors,
        shapes = ListItemDefaults.segmentedShapes(
            index = index,
            count = totalCount,
            defaultShapes = ListItemDefaults.shapes()
        ),
        content = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = category.iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text = stringResource(id = category.nameRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExpanded)
                                    MaterialTheme.colorScheme.surface
                                else
                                    MaterialTheme.colorScheme.surfaceContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isExpanded)
                                    R.drawable.ic_keyboard_arrow_up_filled
                                else
                                    R.drawable.ic_keyboard_arrow_down_filled
                            ),
                            contentDescription = null,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        text = stringResource(id = category.descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 2.dp)
    )
}

data class CategoryItem(
    val nameRes: Int,
    val iconRes: Int,
    val descriptionRes: Int
)

@Preview(showBackground = true)
@Composable
fun CategoriesDescriptionPreview() {
    MyFinanceTheme {
        CategoriesDescriptionScreen(onBackClick = {})
    }
}
