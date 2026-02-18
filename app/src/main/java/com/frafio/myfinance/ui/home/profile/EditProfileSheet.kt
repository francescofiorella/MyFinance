package com.frafio.myfinance.ui.home.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.composable.components.SheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun EditProfileSheet(
    fullName: String,
    onDismiss: () -> Unit,
    onEditFullName: (String) -> Unit,
    showSnackbar: (String) -> Unit,
) {
    var isEditingName by remember { mutableStateOf(false) }
    var nameTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = fullName,
                selection = TextRange(fullName.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    SheetDialog(
        icon = R.drawable.ic_person_filled,
        title = R.string.your_profile,
        label = R.string.edit
    ) {
        Column {
            // Upload profile picture item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showSnackbar("Coming soon!")
                        onDismiss()
                    },
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

            // Edit full name item
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isEditingName) {
                        isEditingName = true
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_outline),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 15.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    if (isEditingName) {
                        TextField(
                            value = nameTextFieldValue,
                            onValueChange = { nameTextFieldValue = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            placeholder = { Text(stringResource(id = R.string.signup_name)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                            ),
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
                    } else {
                        Text(
                            text = stringResource(id = R.string.edit_full_name),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfileSheetPreview() {
    MyFinanceTheme {
        EditProfileSheet(
            fullName = "John Doe",
            onDismiss = {},
            onEditFullName = {},
            showSnackbar = {}
        )
    }
}
