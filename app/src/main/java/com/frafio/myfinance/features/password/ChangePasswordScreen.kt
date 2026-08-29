package com.frafio.myfinance.features.password

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.SwipeableSnackbarHost
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.core.theme.MyFinanceTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    appState: MyFinanceAppState,
    viewModel: ChangePasswordViewModel,
    onBackClick: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val hasPassword = user?.hasPassword ?: false

    val currentPasswordState = rememberTextFieldState()
    val newPasswordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var remoteCurrentPasswordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentPasswordState.text) {
        currentPasswordError = null
        remoteCurrentPasswordError = null
    }

    LaunchedEffect(newPasswordState.text) {
        newPasswordError = null
    }

    LaunchedEffect(confirmPasswordState.text) {
        confirmPasswordError = null
    }

    LaunchedEffect(viewModel.uiEvents) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is ChangePasswordUiEvent.Success -> {
                    onBackClick()
                    appState.showSnackBar(event.message)
                }

                is ChangePasswordUiEvent.InvalidCurrentPassword -> {
                    remoteCurrentPasswordError = event.message
                }

                is ChangePasswordUiEvent.ShowSnackBar -> {
                    appState.showSnackBar(event.message)
                }
            }
        }
    }

    ChangePasswordScreen(
        appState = appState,
        hasPassword = hasPassword,
        isLoading = isLoading,
        currentPasswordState = currentPasswordState,
        currentPasswordVisible = currentPasswordVisible,
        onCurrentPasswordVisibleChange = { currentPasswordVisible = it },
        newPasswordState = newPasswordState,
        newPasswordVisible = newPasswordVisible,
        onNewPasswordVisibleChange = { newPasswordVisible = it },
        confirmPasswordState = confirmPasswordState,
        confirmPasswordVisible = confirmPasswordVisible,
        onConfirmPasswordVisibleChange = { confirmPasswordVisible = it },
        onSaveClick = {
            var hasLocalError = false
            val currentPassword = currentPasswordState.text.toString()
            val newPassword = newPasswordState.text.toString()
            val confirmPassword = confirmPasswordState.text.toString()

            if (hasPassword) {
                if (currentPassword.isEmpty()) {
                    currentPasswordError = AuthResult(AuthCode.EMPTY_PASSWORD).message
                    hasLocalError = true
                } else if (currentPassword.length < 8) {
                    currentPasswordError = AuthResult(AuthCode.SHORT_PASSWORD).message
                    hasLocalError = true
                }
            }

            if (newPassword.isEmpty()) {
                newPasswordError = AuthResult(AuthCode.EMPTY_PASSWORD).message
                hasLocalError = true
            } else if (newPassword.length < 8) {
                newPasswordError = AuthResult(AuthCode.SHORT_PASSWORD).message
                hasLocalError = true
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordError = AuthResult(AuthCode.EMPTY_CONFIRM_PASSWORD).message
                hasLocalError = true
            } else if (confirmPassword != newPassword) {
                confirmPasswordError = AuthResult(AuthCode.PASSWORD_NOT_MATCH).message
                hasLocalError = true
            }

            if (!hasLocalError) {
                viewModel.changePassword(newPassword, if (hasPassword) currentPassword else null)
            }
        },
        onBackClick = onBackClick,
        currentPasswordError = currentPasswordError,
        newPasswordError = newPasswordError,
        confirmPasswordError = confirmPasswordError,
        remoteCurrentPasswordError = remoteCurrentPasswordError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    appState: MyFinanceAppState,
    hasPassword: Boolean,
    isLoading: Boolean,
    currentPasswordState: TextFieldState,
    currentPasswordVisible: Boolean,
    onCurrentPasswordVisibleChange: (Boolean) -> Unit,
    newPasswordState: TextFieldState,
    newPasswordVisible: Boolean,
    onNewPasswordVisibleChange: (Boolean) -> Unit,
    confirmPasswordState: TextFieldState,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibleChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    currentPasswordError: String? = null,
    newPasswordError: String? = null,
    confirmPasswordError: String? = null,
    remoteCurrentPasswordError: String? = null
) {
    val isSaveEnabled = !isLoading &&
            newPasswordState.text.isNotEmpty() &&
            confirmPasswordState.text.isNotEmpty() &&
            (!hasPassword || currentPasswordState.text.isNotEmpty())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = {
            SwipeableSnackbarHost(hostState = appState.snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ChangePasswordTopBar(
                onBackClick = onBackClick,
                onSaveClick = onSaveClick,
                isSaveEnabled = isSaveEnabled
            )

            Column(
                modifier = Modifier
                    .widthIn(max = BottomSheetDefaults.SheetMaxWidth)
                    .align(Alignment.CenterHorizontally)
                    .verticalScroll(rememberScrollState())
                    .animateContentSize()
            ) {
                ChangePasswordForm(
                    hasPassword = hasPassword,
                    isLoading = isLoading,
                    currentPasswordState = currentPasswordState,
                    currentPasswordVisible = currentPasswordVisible,
                    onCurrentPasswordVisibleChange = onCurrentPasswordVisibleChange,
                    newPasswordState = newPasswordState,
                    newPasswordVisible = newPasswordVisible,
                    onNewPasswordVisibleChange = onNewPasswordVisibleChange,
                    confirmPasswordState = confirmPasswordState,
                    confirmPasswordVisible = confirmPasswordVisible,
                    onConfirmPasswordVisibleChange = onConfirmPasswordVisibleChange,
                    onSaveClick = onSaveClick,
                    currentPasswordError = currentPasswordError,
                    newPasswordError = newPasswordError,
                    confirmPasswordError = confirmPasswordError,
                    remoteCurrentPasswordError = remoteCurrentPasswordError
                )
            }
        }
    }
}

@Composable
fun ChangePasswordTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onBackClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shapes = IconButtonDefaults.shapes(),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close_filled),
                contentDescription = stringResource(id = R.string.back_arrow),
            )
        }

        Text(
            text = stringResource(id = R.string.password),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        FilledIconButton(
            modifier = Modifier
                .padding(end = 4.dp)
                .width(52.dp),
            onClick = {
                focusManager.clearFocus()
                onSaveClick()
            },
            enabled = isSaveEnabled,
            shapes = IconButtonDefaults.shapes()
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check_filled),
                contentDescription = stringResource(R.string.save),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordForm(
    hasPassword: Boolean,
    isLoading: Boolean,
    currentPasswordState: TextFieldState,
    currentPasswordVisible: Boolean,
    onCurrentPasswordVisibleChange: (Boolean) -> Unit,
    newPasswordState: TextFieldState,
    newPasswordVisible: Boolean,
    onNewPasswordVisibleChange: (Boolean) -> Unit,
    confirmPasswordState: TextFieldState,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibleChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    currentPasswordError: String? = null,
    newPasswordError: String? = null,
    confirmPasswordError: String? = null,
    remoteCurrentPasswordError: String? = null
) {
    val colors =
        ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    Column {
        if (hasPassword) {
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(
                    index = 0,
                    count = 1,
                    defaultShapes = ListItemDefaults.shapes()
                ),
                colors = colors,
                content = {
                    PasswordField(
                        state = currentPasswordState,
                        label = stringResource(id = R.string.current_password),
                        isVisible = currentPasswordVisible,
                        onVisibilityToggle = { onCurrentPasswordVisibleChange(!currentPasswordVisible) },
                        imeAction = ImeAction.Next,
                        icon = R.drawable.ic_key_filled,
                        iconShape = MaterialShapes.Cookie12Sided.toShape(),
                        enabled = !isLoading,
                        error = remoteCurrentPasswordError ?: currentPasswordError,
                        contentType = ContentType.Password
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
        }

        SegmentedListItem(
            shapes = ListItemDefaults.segmentedShapes(
                index = 0,
                count = 2,
                defaultShapes = ListItemDefaults.shapes()
            ),
            colors = colors,
            content = {
                PasswordField(
                    state = newPasswordState,
                    label = stringResource(id = R.string.signup_password),
                    isVisible = newPasswordVisible,
                    onVisibilityToggle = { onNewPasswordVisibleChange(!newPasswordVisible) },
                    imeAction = ImeAction.Next,
                    icon = R.drawable.ic_password_filled,
                    iconShape = MaterialShapes.Cookie7Sided.toShape(),
                    enabled = !isLoading,
                    error = newPasswordError,
                    contentType = ContentType.NewPassword
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 2.dp)
        )

        SegmentedListItem(
            shapes = ListItemDefaults.segmentedShapes(
                index = 1,
                count = 2,
                defaultShapes = ListItemDefaults.shapes()
            ),
            colors = colors,
            content = {
                PasswordField(
                    state = confirmPasswordState,
                    label = stringResource(id = R.string.signup_password_confirm),
                    isVisible = confirmPasswordVisible,
                    onVisibilityToggle = { onConfirmPasswordVisibleChange(!confirmPasswordVisible) },
                    imeAction = ImeAction.Done,
                    icon = R.drawable.ic_password_2_filled,
                    iconShape = MaterialShapes.Cookie7Sided.toShape(),
                    enabled = !isLoading,
                    onDone = onSaveClick,
                    error = confirmPasswordError,
                    contentType = ContentType.NewPassword
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PasswordField(
    state: TextFieldState,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    imeAction: ImeAction,
    icon: Int,
    iconShape: Shape = MaterialShapes.Circle.toShape(),
    enabled: Boolean = true,
    onDone: () -> Unit = {},
    error: String? = null,
    contentType: ContentType? = null
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(48.dp)
                .clip(iconShape)
                .background(if (enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.38f
                )
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            SecureTextField(
                state = state,
                enabled = enabled,
                placeholder = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (contentType != null) Modifier.semantics {
                            this.contentType = contentType
                        } else Modifier
                    ),
                textStyle = MaterialTheme.typography.bodyLarge,
                textObfuscationMode = if (isVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                trailingIcon = {
                    IconButton(onClick = onVisibilityToggle) {
                        Icon(
                            painter = painterResource(id = if (isVisible) R.drawable.ic_visibility_filled else R.drawable.ic_visibility_off_filled),
                            contentDescription = if (isVisible) "Hide password" else "Show password"
                        )
                    }
                },
                isError = error != null,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = imeAction,
                    autoCorrectEnabled = false
                ),
                onKeyboardAction = {
                    if (imeAction == ImeAction.Next) focusManager.moveFocus(FocusDirection.Down)
                    else if (imeAction == ImeAction.Done) {
                        focusManager.clearFocus()
                        onDone()
                    }
                }
            )
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordPreview() {
    MyFinanceTheme {
        val appState = rememberMyFinanceAppState()
        ChangePasswordScreen(
            appState = appState,
            hasPassword = true,
            isLoading = false,
            currentPasswordState = rememberTextFieldState("oldpassword"),
            currentPasswordVisible = false,
            onCurrentPasswordVisibleChange = {},
            newPasswordState = rememberTextFieldState("newpassword"),
            newPasswordVisible = false,
            onNewPasswordVisibleChange = {},
            confirmPasswordState = rememberTextFieldState("newpassword"),
            confirmPasswordVisible = false,
            onConfirmPasswordVisibleChange = {},
            onSaveClick = {},
            onBackClick = {},
            currentPasswordError = null,
            newPasswordError = null,
            confirmPasswordError = null,
            remoteCurrentPasswordError = null
        )
    }
}
