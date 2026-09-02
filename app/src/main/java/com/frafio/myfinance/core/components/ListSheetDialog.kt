package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
            ListSheetItem(
                item = item,
                onDismiss = onDismiss
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
