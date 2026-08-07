package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.MenuItem
import com.frafio.myfinance.core.theme.GoogleSansFlexRoundFamily
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun GridSheetDialog(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    title: String,
    label: String,
    labelFirst: Boolean = true,
    endContent: String? = null,
    rowSize: Int,
    items: List<MenuItem>,
    onDismiss: () -> Unit,
    bottomContent: @Composable () -> Unit = {}
) {
    SheetDialog(
        icon = icon,
        title = title,
        label = label,
        labelFirst = labelFirst,
        endContent = endContent,
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(rowSize),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                GridItem(
                    item = item,
                    onDismiss = onDismiss
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                bottomContent()
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (item.symbol != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    text = item.symbol!!,
                    autoSize = TextAutoSize.StepBased(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = GoogleSansFlexRoundFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = item.text ?: stringResource(id = item.textRes),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            rowSize = 3,
            items = listOf(
                MenuItem(
                    iconRes = R.drawable.ic_home_filled,
                    textRes = R.string.housing,
                    enabled = false
                ) {},
                MenuItem(
                    iconRes = R.drawable.ic_shopping_cart_filled,
                    textRes = R.string.groceries,
                    enabled = true
                ) {},
                MenuItem(
                    symbol = "€",
                    text = "Euro",
                    enabled = true
                ) {}
            ),
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
