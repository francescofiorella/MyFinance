package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.theme.MyFinanceTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyListItem(
    messageRes: Int,
    modifier: Modifier = Modifier
) {
    val colors = ListItemDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
    SegmentedListItem(
        onClick = {},
        onLongClick = {},
        shapes = ListItemDefaults.shapes(
            shape = ListItemDefaults.shapes().selectedShape
        ),
        colors = colors,
        content = {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun EmptyListItemPreview() {
    MyFinanceTheme {
        EmptyListItem(messageRes = R.string.no_expenses)
    }
}
