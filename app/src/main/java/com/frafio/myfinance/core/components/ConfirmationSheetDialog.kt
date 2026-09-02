package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.MenuItem
import com.frafio.myfinance.core.theme.MyFinanceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationSheetDialog(
    modifier: Modifier = Modifier,
    @StringRes headerText: Int,
    @DrawableRes actionIcon: Int,
    @StringRes actionText: Int,
    onActionClick: () -> Unit,
    show: Boolean,
    onDismiss: () -> Unit
) {
    AdaptiveSheet(
        show = show,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()
                .padding(vertical = 24.dp)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(id = headerText),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            ListSheetItem(
                item = MenuItem(
                    iconRes = actionIcon,
                    textRes = actionText,
                    onClick = onActionClick
                ),
                onDismiss = onDismiss
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationSheetDialogPreview() {
    MyFinanceTheme {
        ConfirmationSheetDialog(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow),
            headerText = R.string.delete_confirmation,
            actionIcon = R.drawable.ic_delete_outline,
            actionText = R.string.delete_permanently,
            onActionClick = {},
            show = true,
            onDismiss = {}
        )
    }
}
