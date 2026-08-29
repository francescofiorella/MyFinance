package com.frafio.myfinance.features.password.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.features.password.ChangePasswordScreen
import com.frafio.myfinance.features.password.ChangePasswordViewModel
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey

fun EntryProviderScope<NavKey>.changePasswordEntry(
    appState: MyFinanceAppState,
    onBackClick: () -> Unit
) {
    entry<RootKey.ChangePassword> {
        val viewModel: ChangePasswordViewModel = hiltViewModel()

        ChangePasswordScreen(
            appState = appState,
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}
