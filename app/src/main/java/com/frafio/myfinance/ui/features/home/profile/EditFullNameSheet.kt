package com.frafio.myfinance.ui.features.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.components.SheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun EditFullNameSheet(
    fullName: String,
    onDismiss: () -> Unit,
    onEditFullName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var nameTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = fullName,
                selection = TextRange(fullName.length)
            )
        )
    }

    SheetDialog(
        icon = R.drawable.ic_person_filled,
        title = stringResource(id = R.string.your_name),
        label = stringResource(id = R.string.edit),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = nameTextFieldValue,
                onValueChange = { nameTextFieldValue = it },
                modifier = Modifier
                    .weight(1f),
                label = { Text(stringResource(id = R.string.signup_name)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_outline),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (nameTextFieldValue.text.trim()
                            .isNotEmpty() && nameTextFieldValue.text.trim() != fullName
                    ) {
                        onEditFullName(nameTextFieldValue.text.trim())
                        onDismiss()
                    }
                })
            )
            Spacer(modifier = Modifier.width(5.dp))
            FilledTonalIconButton(
                onClick = {
                    onEditFullName(nameTextFieldValue.text.trim())
                    onDismiss()
                },
                enabled = nameTextFieldValue.text.trim()
                    .isNotEmpty() && nameTextFieldValue.text.trim() != fullName
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_filled),
                    contentDescription = stringResource(id = R.string.confirm)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditFullNameSheetPreview() {
    MyFinanceTheme {
        EditFullNameSheet(
            fullName = "John Doe",
            onDismiss = {},
            onEditFullName = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
