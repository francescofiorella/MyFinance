package com.frafio.myfinance.features.auth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthForm(
    isSigningUp: Boolean,
    isLoading: Boolean,
    emailState: TextFieldState,
    passwordState: TextFieldState,
    fullNameState: TextFieldState,
    confirmPasswordState: TextFieldState,
    emailError: String?,
    passwordError: String?,
    fullNameError: String?,
    confirmPasswordError: String?,
    onAuthClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val submit = { focusManager.clearFocus(); onAuthClick() }

    Column(
        modifier = modifier
            .widthIn(max = BottomSheetDefaults.SheetMaxWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = isSigningUp,
            enter = fadeIn(MaterialTheme.motionScheme.fastSpatialSpec()) + expandVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + slideInVertically(MaterialTheme.motionScheme.fastSpatialSpec()),
            exit = fadeOut(MaterialTheme.motionScheme.fastSpatialSpec()) + shrinkVertically(MaterialTheme.motionScheme.fastSpatialSpec()) + slideOutVertically(MaterialTheme.motionScheme.fastSpatialSpec())
        ) {
            AuthTextField(
                state = fullNameState,
                label = stringResource(id = R.string.signup_name),
                error = fullNameError,
                enabled = !isLoading,
                icon = R.drawable.ic_person_filled,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                contentType = ContentType.PersonFullName
            )
        }

        AuthTextField(
            state = emailState,
            label = stringResource(id = R.string.login_signup_email),
            error = emailError,
            enabled = !isLoading,
            icon = R.drawable.ic_mail_filled,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            contentType = ContentType.EmailAddress,
            testTag = "email_field"
        )

        var passwordVisible by remember { mutableStateOf(false) }
        AuthPasswordField(
            state = passwordState,
            label = stringResource(
                id = if (isSigningUp) R.string.signup_password else R.string.login_password
            ),
            error = passwordError,
            enabled = !isLoading,
            icon = R.drawable.ic_password_filled,
            passwordVisible = passwordVisible,
            onPasswordVisibleChange = { passwordVisible = it },
            keyboardOptions = KeyboardOptions(imeAction = if (isSigningUp) ImeAction.Next else ImeAction.Done),
            // Next keeps its default focus move; Done submits the login.
            onKeyboardAction = { performDefaultAction -> if (isSigningUp) performDefaultAction() else submit() },
            contentType = if (isSigningUp) ContentType.NewPassword else ContentType.Password,
            testTag = "password_field"
        )

        AnimatedVisibility(
            visible = isSigningUp,
            enter = fadeIn() + expandVertically() + slideInVertically(),
            exit = fadeOut() + shrinkVertically() + slideOutVertically()
        ) {
            var confirmPasswordVisible by remember { mutableStateOf(false) }
            AuthPasswordField(
                state = confirmPasswordState,
                label = stringResource(id = R.string.signup_password_confirm),
                error = confirmPasswordError,
                enabled = !isLoading,
                icon = R.drawable.ic_password_2_filled,
                passwordVisible = confirmPasswordVisible,
                onPasswordVisibleChange = { confirmPasswordVisible = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                onKeyboardAction = { submit() },
                contentType = ContentType.NewPassword
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthFormLoginPreview() {
    MyFinanceTheme {
        AuthForm(
            isSigningUp = false,
            isLoading = false,
            emailState = rememberTextFieldState(),
            passwordState = rememberTextFieldState(),
            fullNameState = rememberTextFieldState(),
            confirmPasswordState = rememberTextFieldState(),
            emailError = null,
            passwordError = null,
            fullNameError = null,
            confirmPasswordError = null,
            onAuthClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthFormSignupPreview() {
    MyFinanceTheme {
        AuthForm(
            isSigningUp = true,
            isLoading = false,
            emailState = rememberTextFieldState(),
            passwordState = rememberTextFieldState(),
            fullNameState = rememberTextFieldState(),
            confirmPasswordState = rememberTextFieldState(),
            emailError = "Invalid email",
            passwordError = null,
            fullNameError = null,
            confirmPasswordError = null,
            onAuthClick = {}
        )
    }
}
