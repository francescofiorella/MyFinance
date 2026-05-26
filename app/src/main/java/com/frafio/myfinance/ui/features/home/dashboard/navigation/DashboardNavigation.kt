package com.frafio.myfinance.ui.features.home.dashboard.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.ui.features.home.dashboard.DashboardScreen
import com.frafio.myfinance.ui.home.dashboard.DashboardViewModel
import com.frafio.myfinance.ui.navigation.MyFinanceAppState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey

fun EntryProviderScope<NavKey>.dashboardEntry(appState: MyFinanceAppState) {
    entry<MyFinanceNavKey.Dashboard> {
        val viewModel: DashboardViewModel = hiltViewModel()

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
