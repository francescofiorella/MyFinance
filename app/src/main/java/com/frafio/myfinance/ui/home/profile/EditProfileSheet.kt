package com.frafio.myfinance.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.composable.components.SheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun EditProfileSheet(
    onDismiss: () -> Unit,
    onUploadProPic: () -> Unit,
    onEditFullName: () -> Unit,
    modifier: Modifier = Modifier
) {
    SheetDialog(
        icon = R.drawable.ic_person_filled,
        title = stringResource(id = R.string.your_profile),
        label = stringResource(id = R.string.edit),
        modifier = modifier
    ) {
        Column {
            Surface(
                onClick = {
                    onUploadProPic()
                    onDismiss()
                },
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                // Upload profile picture item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_upload_filled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 15.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = stringResource(id = R.string.edit_propic),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }

        // Edit full name item
        Surface(
            onClick = {
                onEditFullName()
                onDismiss()
            },
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_outline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 15.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = stringResource(id = R.string.edit_full_name),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
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
