package com.frafio.myfinance.ui.components

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.MenuItem
import com.frafio.myfinance.ui.theme.MyFinanceTheme


@Composable
fun GridSheetDialog(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    label: String,
    labelFirst: Boolean = true,
    endContent: String? = null,
    rowSize: Int,
    items: List<MenuItem>,
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
            items.chunked(rowSize).forEach { rowItems ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { item ->
                        GridItem(
                            modifier = Modifier.weight(1f),
                            item = item,
                            onDismiss = onDismiss
                        )
                    }
                    // Fill remaining space if row is not full
                    if (rowItems.size < rowSize) {
                        repeat(rowSize - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridItem(
    modifier: Modifier = Modifier,
    item: MenuItem,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = modifier.alpha(if (item.enabled) 1f else 0.38f),
        onClick = {
            item.onClick()
            onDismiss()
        },
        enabled = item.enabled,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(id = item.textRes),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GridSheetPreview() {
    MyFinanceTheme {
        GridSheetDialog(
            icon = R.drawable.ic_grid_3x3_filled,
            title = stringResource(id = R.string.category),
            label = stringResource(id = R.string.select),
            onDismiss = {},
            rowSize = 2,
            items = listOf(
                MenuItem(
                    iconRes = R.drawable.ic_home_filled,
                    textRes = R.string.housing,
                    enabled = false
                ) {},
                MenuItem(
                    iconRes = R.drawable.ic_shopping_cart_filled,
                    textRes = R.string.groceries
                ) {}
            ),
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
