package com.frafio.myfinance.features.expenses.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.frafio.myfinance.core.components.AdaptiveSheet
import com.frafio.myfinance.core.components.SheetDialog
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.utils.getCategoryIcon

@Composable
fun LabelsSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    expense: Expense? = null,
    labels: List<String> = listOf(),
    selectedLabels: List<String> = listOf(),
    onLabelCheckedChanged: (String, Boolean) -> Unit,
) {
    @DrawableRes val icon = if (expense != null)
        getCategoryIcon(expense.category)
    else
        R.drawable.ic_sell_filled
    val title = expense?.name ?: stringResource(id = R.string.labels)
    val dialogLabel = expense?.getDateString() ?: stringResource(id = R.string.select)
    val labelFirst = expense == null
    val endContent = expense?.getPriceString()

    AdaptiveSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        SheetDialog(
            modifier = modifier,
            icon = icon,
            label = dialogLabel,
            title = title,
            labelFirst = labelFirst,
            endContent = endContent
        ) {
            Column(modifier = Modifier.animateContentSize()) {
                labels.forEachIndexed { index, label ->
                    key(label) {
                        LabelItem(
                            label = label,
                            initialSelected = expense?.labels?.contains(label)
                                ?: selectedLabels.contains(label),
                            onLabelCheckedChanged = onLabelCheckedChanged,
                            index = index,
                            count = labels.size
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LabelItem(
    modifier: Modifier = Modifier,
    label: String,
    initialSelected: Boolean,
    onLabelCheckedChanged: (String, Boolean) -> Unit,
    index: Int,
    count: Int
) {
    var checked by remember(label, initialSelected) {
        mutableStateOf(initialSelected)
    }

    AnimatedVisibility(
        visible = true,
        enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
        exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
    ) {
        SegmentedListItem(
            checked = checked,
            onCheckedChange = {
                onLabelCheckedChanged(label, it)
                checked = it
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shapes = if (count > 1) {
                ListItemDefaults.segmentedShapes(
                    index = index,
                    count = count
                )
            } else {
                ListItemDefaults.shapes(
                    shape = ListItemDefaults.shapes().selectedShape
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (checked)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sell_outline),
                        contentDescription = null,
                        tint = if (checked)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            content = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingContent = {
                Checkbox(
                    modifier = Modifier.padding(8.dp),
                    checked = checked,
                    onCheckedChange = null
                )
            },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(
                    bottom = if (index == count - 1)
                        0.dp
                    else
                        2.dp
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LabelsSheetPreview() {
    MyFinanceTheme {
        LabelsSheet(
            show = true,
            onDismiss = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow),
            labels = listOf(
                "Travel",
                "Dinner"
            ),
            onLabelCheckedChanged = { _, _ -> },
        )
    }
}
