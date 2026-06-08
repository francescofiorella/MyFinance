package com.frafio.myfinance.features.dashboard.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.features.dashboard.DashboardScreen
import com.frafio.myfinance.features.dashboard.DashboardViewModel
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.HomeTabKey

fun EntryProviderScope<NavKey>.dashboardEntry(appState: MyFinanceAppState) {
    entry<HomeTabKey.Dashboard> {
        val viewModel: DashboardViewModel = hiltViewModel()

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == HomeTabKey.Dashboard) {
                    viewModel.scrollToTop()
                }
            }
        }

        DashboardScreen(viewModel = viewModel)
    }
}
