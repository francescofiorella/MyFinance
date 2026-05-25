package com.frafio.myfinance.ui.features.home.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.ui.features.home.profile.ProfileScreen
import com.frafio.myfinance.ui.home.profile.ProfileViewModel
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey

fun EntryProviderScope<NavKey>.profileEntry(
    viewModel: ProfileViewModel,
    onUploadProPic: () -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
) {
    entry<MyFinanceNavKey.Profile> {
        ProfileScreen(
            viewModel = viewModel,
            onUploadProPic = onUploadProPic,
            onDynamicColorChanged = onDynamicColorChanged
        )
    }
}
