package com.frafio.myfinance.features.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R

@Composable
fun AuthTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    label: String,
    error: String?,
    enabled: Boolean,
    icon: Int,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    contentType: ContentType? = null,
    testTag: String? = null
) {
    AuthFieldLayout(modifier = modifier, error = error) {
        OutlinedTextField(
            state = state,
            modifier = Modifier.authField(testTag, contentType),
            enabled = enabled,
            label = { Text(label) },
            leadingIcon = { Icon(painterResource(id = icon), contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    ErrorIcon()
                } else if (state.text.isNotEmpty() && enabled) {
                    IconButton(onClick = { state.clearText() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_cancel_filled), contentDescription = stringResource(R.string.clear))
                    }
                }
            },
            isError = error != null,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = TextFieldLineLimits.SingleLine,
            shape = MaterialTheme.shapes.largeIncreased
        )
    }
}

@Composable
fun AuthPasswordField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    label: String,
    error: String?,
    enabled: Boolean,
    icon: Int,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    contentType: ContentType? = null,
    testTag: String? = null
) {
    AuthFieldLayout(modifier = modifier, error = error) {
        OutlinedSecureTextField(
            state = state,
            modifier = Modifier.authField(testTag, contentType),
            enabled = enabled,
            label = { Text(label) },
            leadingIcon = { Icon(painterResource(id = icon), contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    ErrorIcon()
                } else {
                    IconButton(onClick = { onPasswordVisibleChange(!passwordVisible) }) {
                        Icon(
                            painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility_filled else R.drawable.ic_visibility_off_filled),
                            contentDescription = stringResource(if (passwordVisible) R.string.hide_password else R.string.show_password)
                        )
                    }
                }
            },
            isError = error != null,
            textObfuscationMode = if (passwordVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
            // A password keyboard never learns or suggests what is typed, whatever the caller passed.
            keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
            onKeyboardAction = onKeyboardAction,
            shape = MaterialTheme.shapes.largeIncreased
        )
    }
}

@Composable
private fun AuthFieldLayout(
    modifier: Modifier,
    error: String?,
    field: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        field()

        AnimatedVisibility(
            visible = error != null,
            enter = expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeIn(MaterialTheme.motionScheme.fastSpatialSpec()),
            exit = shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + fadeOut(MaterialTheme.motionScheme.fastSpatialSpec())
        ) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun ErrorIcon() {
    Icon(painter = painterResource(id = R.drawable.ic_error_filled), contentDescription = null)
}

private fun Modifier.authField(testTag: String?, contentType: ContentType?): Modifier = this
    .fillMaxWidth()
    .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    .then(if (contentType != null) Modifier.semantics { this.contentType = contentType } else Modifier)
