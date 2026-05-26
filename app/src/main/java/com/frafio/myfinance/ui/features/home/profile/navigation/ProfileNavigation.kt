package com.frafio.myfinance.ui.features.home.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import android.app.Application
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
    onDynamicColorChanged: (Boolean) -> Unit,
) {
    entry<MyFinanceNavKey.Profile> {
        val context = LocalContext.current
        val viewModel: ProfileViewModel = viewModel(
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
        )
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

                override fun onProfileUpdateComplete(response: androidx.lifecycle.LiveData<com.frafio.myfinance.data.model.AuthResult>) {
                    response.observeForever { authResult ->
                        appState.showProgress = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(authResult.message)
                        }
                        if (authResult.code == com.frafio.myfinance.data.enums.auth.AuthCode.USER_DATA_UPDATED.code) {
                            viewModel.updateLocalUser()
                        }
                    }
                }
            }
            onDispose { viewModel.listener = null }
        }

        ProfileScreen(
            viewModel = viewModel,
            onUploadProPic = onUploadProPic,
            onDynamicColorChanged = onDynamicColorChanged
        )
    }
}
