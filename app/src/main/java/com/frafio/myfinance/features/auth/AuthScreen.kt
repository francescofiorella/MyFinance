package com.frafio.myfinance.features.auth

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.core.theme.MyFinanceTheme
import com.frafio.myfinance.core.components.SwipeableSnackbarHost
import com.frafio.myfinance.features.auth.components.AuthForm
import com.frafio.myfinance.features.auth.components.GoogleSignInButton
import com.frafio.myfinance.features.auth.components.ResetPasswordSheet
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    appState: MyFinanceAppState,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isSigningUp) {
        focusManager.clearFocus()
        viewModel.onToggleAuthMode()
    }

    AuthContent(
        appState = appState,
        uiState = uiState,
        isLoading = isLoading,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onFullNameChange = viewModel::onFullNameChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onToggleAuthMode = {
            focusManager.clearFocus()
            viewModel.onToggleAuthMode()
        },
        onAuthClick = {
            focusManager.clearFocus()
            if (uiState.isSigningUp) viewModel.onSignupButtonClick()
            else viewModel.onLoginButtonClick()
        },
        onGoogleClick = {
            focusManager.clearFocus()
            scope.launch {
                viewModel.startLoading()
                handleGoogleSignIn(context, viewModel::onGoogleRequest) { message ->
                    appState.showSnackBar(message)
                    viewModel.stopLoading()
                }
            }
        },
        onForgotPasswordClick = {
            focusManager.clearFocus()
            viewModel.setShowResetPasswordSheet(true)
        }
    )

    ResetPasswordSheet(
        show = uiState.showResetPasswordSheet,
        onDismiss = { viewModel.setShowResetPasswordSheet(false) },
        onSend = { email ->
            viewModel.resetPassword(email)
        }
    )
}

private suspend fun handleGoogleSignIn(
    context: Context,
    onSuccess: (Credential) -> Unit,
    onMessage: (String) -> Unit
) {
    val credentialManager = CredentialManager.create(context)
    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .setFilterByAuthorizedAccounts(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(context = context, request = request)
        onSuccess(result.credential)
    } catch (_: GetCredentialException) {
        onMessage(AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE).message)
    } catch (_: NoCredentialException) {
        onMessage(AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE).message)
    }
}

@Composable
private fun AuthContent(
    appState: MyFinanceAppState,
    uiState: AuthUiState,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleAuthMode: () -> Unit,
    onAuthClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SwipeableSnackbarHost(hostState = appState.snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onToggleAuthMode,
                    enabled = !isLoading
                ) {
                    Text(
                        text = stringResource(
                            id = if (uiState.isSigningUp) R.string.signup_login else R.string.login_signup
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val topPadding = maxHeight * 0.2f
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(topPadding))

                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.displayMediumEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthForm(
                        isSigningUp = uiState.isSigningUp,
                        isLoading = isLoading,
                        email = uiState.email,
                        onEmailChange = onEmailChange,
                        password = uiState.password,
                        onPasswordChange = onPasswordChange,
                        fullName = uiState.fullName,
                        onFullNameChange = onFullNameChange,
                        confirmPassword = uiState.confirmPassword,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        emailError = uiState.emailError,
                        passwordError = uiState.passwordError,
                        fullNameError = uiState.fullNameError,
                        confirmPasswordError = uiState.confirmPasswordError,
                        onAuthClick = onAuthClick
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAuthClick,
                        enabled = !isLoading
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_login_filled),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(id = if (uiState.isSigningUp) R.string.signup else R.string.login))
                    }

                    TextButton(
                        onClick = onForgotPasswordClick,
                        enabled = !uiState.isSigningUp && !isLoading
                    ) {
                        Text(
                            text = stringResource(
                                id = if (uiState.isSigningUp) R.string.auth_or else R.string.forgotten_password
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    GoogleSignInButton(
                        onClick = onGoogleClick,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            AnimatedVisibility(
                visible = uiState.isSigningUp,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onToggleAuthMode,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    enabled = !isLoading
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back_filled),
                        contentDescription = stringResource(id = R.string.back_arrow)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    MyFinanceTheme {
        val appState = rememberMyFinanceAppState()
        AuthContent(
            appState = appState,
            uiState = AuthUiState(),
            isLoading = false,
            onEmailChange = {},
            onPasswordChange = {},
            onFullNameChange = {},
            onConfirmPasswordChange = {},
            onToggleAuthMode = {},
            onAuthClick = {},
            onGoogleClick = {},
            onForgotPasswordClick = {}
        )
    }
}
