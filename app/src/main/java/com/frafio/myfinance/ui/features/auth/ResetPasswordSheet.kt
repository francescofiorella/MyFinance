package com.frafio.myfinance.ui.features.auth

import android.util.Patterns
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.ui.components.SheetDialog
import com.frafio.myfinance.ui.theme.MyFinanceTheme

@Composable
fun ResetPasswordSheet(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var emailFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
            )
        )
    }

    val isEmailValid by remember(emailFieldValue.text) {
        derivedStateOf {
            Patterns.EMAIL_ADDRESS.matcher(emailFieldValue.text.trim()).matches()
        }
    }

    SheetDialog(
        icon = R.drawable.ic_password_filled,
        title = stringResource(id = R.string.password),
        label = stringResource(id = R.string.reset),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = emailFieldValue,
                onValueChange = { emailFieldValue = it },
                modifier = Modifier
                    .weight(1f),
                label = { Text(stringResource(id = R.string.login_signup_email)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mail_outline),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Email
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (isEmailValid) {
                        onSend(emailFieldValue.text.trim())
                        onDismiss()
                    }
                })
            )
            Spacer(modifier = Modifier.width(5.dp))
            FilledTonalIconButton(
                onClick = {
                    onSend(emailFieldValue.text.trim())
                    onDismiss()
                },
                enabled = isEmailValid
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_send_outline),
                    contentDescription = stringResource(id = R.string.send)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordSheetPreview() {
    MyFinanceTheme {
        ResetPasswordSheet(
            onDismiss = {},
            onSend = {},
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
