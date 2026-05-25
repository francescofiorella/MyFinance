package com.frafio.myfinance.ui.features.home.dashboard.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.ui.features.home.dashboard.DashboardScreen
import com.frafio.myfinance.ui.home.dashboard.DashboardViewModel
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey

fun EntryProviderScope<NavKey>.dashboardEntry(
    viewModel: DashboardViewModel
) {
    entry<MyFinanceNavKey.Dashboard> {
        DashboardScreen(viewModel = viewModel)
    }
}
