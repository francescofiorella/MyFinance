package com.frafio.myfinance.ui.features.home.dashboard.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import android.app.Application
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.frafio.myfinance.ui.features.home.dashboard.DashboardScreen
import com.frafio.myfinance.ui.home.dashboard.DashboardViewModel
import com.frafio.myfinance.ui.navigation.MyFinanceAppState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey

fun EntryProviderScope<NavKey>.dashboardEntry(appState: MyFinanceAppState) {
    entry<MyFinanceNavKey.Dashboard> {
        val context = LocalContext.current
        val viewModel: DashboardViewModel = viewModel(
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
        )

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == MyFinanceNavKey.Dashboard) {
                    viewModel.scrollToTop()
                }
            }
        }

        DashboardScreen(viewModel = viewModel)
    }
}
