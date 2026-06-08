package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.MenuItem
import com.frafio.myfinance.core.theme.MyFinanceTheme

@Composable
fun ListSheetDialog(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    title: String,
    label: String,
    labelFirst: Boolean = true,
    endContent: String? = null,
    items: List<MenuItem>,
    onDismiss: () -> Unit
) {
    SheetDialog(
        icon = icon,
        title = title,
        label = label,
        labelFirst = labelFirst,
        endContent = endContent,
        modifier = modifier
    ) {
        items.forEach { item ->
            ListItem(
                item = item,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun ListItem(
    item: MenuItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(id = item.textRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListSheetPreview() {
    MyFinanceTheme {
        ListSheetDialog(
            icon = R.drawable.ic_person_filled,
            title = stringResource(id = R.string.your_profile),
            label = stringResource(id = R.string.edit),
            onDismiss = {},
            items = listOf(
                MenuItem(
                    iconRes = R.drawable.ic_upload_filled,
                    textRes = R.string.edit_propic,
                    enabled = false
                ) {},
                MenuItem(
                    iconRes = R.drawable.ic_edit_outline,
                    textRes = R.string.edit_full_name
                ) {}
            ),
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
