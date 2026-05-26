package com.frafio.myfinance.ui.features.home.profile.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.ui.features.home.profile.ProfileScreen
import com.frafio.myfinance.ui.home.profile.ProfileListener
import com.frafio.myfinance.ui.home.profile.ProfileViewModel
import com.frafio.myfinance.ui.navigation.LocalSnackbarHostState
import com.frafio.myfinance.ui.navigation.MyFinanceAppState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import kotlinx.coroutines.launch

fun EntryProviderScope<NavKey>.profileEntry(
    appState: MyFinanceAppState,
    onUploadProPic: () -> Unit,
    restartApplication: () -> Unit
) {
    entry<MyFinanceNavKey.Profile> {
        val appRestartMessage = stringResource(id = R.string.restart_app_changes)
        val appRestartActionText = stringResource(id = R.string.restart)

        val viewModel: ProfileViewModel = hiltViewModel()
        val snackbarHostState = LocalSnackbarHostState.current
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == MyFinanceNavKey.Profile) {
                    viewModel.scrollToTop()
                }
            }
        }

        DisposableEffect(viewModel) {
            viewModel.listener = object : ProfileListener {
                override fun onStarted() {
                    appState.showProgress = true
                }

                override fun onProfileUpdateComplete(response: LiveData<AuthResult>) {
                    response.observeForever { authResult ->
                        appState.showProgress = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(authResult.message)
                        }
                        if (authResult.code == AuthCode.USER_DATA_UPDATED.code) {
                            viewModel.updateLocalUser()
                        }
                    }
                }

                override fun onDynamicColorChanged() {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = appRestartMessage,
                            actionLabel = appRestartActionText,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            restartApplication()
                        }
                    }
                }
            }
            onDispose { viewModel.listener = null }
        }

        ProfileScreen(
            viewModel = viewModel,
            onUploadProPic = onUploadProPic
        )
    }
}
