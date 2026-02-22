package com.frafio.myfinance.ui.features.home.profile

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.R
import com.frafio.myfinance.data.model.MenuItem
import com.frafio.myfinance.ui.components.ListSheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun EditProfileSheet(
    onDismiss: () -> Unit,
    onUploadProPic: () -> Unit,
    onEditFullName: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListSheetDialog(
        icon = R.drawable.ic_person_filled,
        title = stringResource(id = R.string.your_profile),
        label = stringResource(id = R.string.edit),
        onDismiss = onDismiss,
        items = listOf(
            MenuItem(
                iconRes = R.drawable.ic_upload_filled,
                textRes = R.string.edit_propic,
                onClick = onUploadProPic
            ),
            MenuItem(
                iconRes = R.drawable.ic_edit_outline,
                textRes = R.string.edit_full_name,
                onClick = onEditFullName
            )
        ),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun EditProfileSheetPreview() {
    MyFinanceTheme {
        EditProfileSheet(
            onDismiss = {},
            onUploadProPic = {},
            onEditFullName = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
