package com.frafio.myfinance.features.auth.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.auth.AuthScreen
import com.frafio.myfinance.features.auth.AuthUiEvent
import com.frafio.myfinance.features.auth.AuthViewModel

fun EntryProviderScope<NavKey>.authEntry(
    appState: MyFinanceAppState,
    onAuthSuccess: () -> Unit
) {
    entry<RootKey.Auth> {
        val viewModel = hiltViewModel<AuthViewModel>()

        val resources = LocalResources.current
        LaunchedEffect(viewModel.uiEvents) {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    AuthUiEvent.Success -> onAuthSuccess()
                    is AuthUiEvent.Message -> appState.showSnackBar(resources.getString(event.messageRes))
                    is AuthUiEvent.Error -> appState.showSnackBar(resources.getString(event.messageRes))
                }
            }
        }

        AuthScreen(
            appState = appState,
            viewModel = viewModel
        )
    }
}
