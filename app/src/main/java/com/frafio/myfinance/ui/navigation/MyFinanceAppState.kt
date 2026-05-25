package com.frafio.myfinance.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.CoroutineScope

val TOP_LEVEL_NAV_KEYS: Set<NavKey> = setOf(
    MyFinanceNavKey.Dashboard,
    MyFinanceNavKey.Expenses,
    MyFinanceNavKey.Budget,
    MyFinanceNavKey.Profile
)

@Composable
fun rememberMyFinanceAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MyFinanceAppState {
    val navigationState = rememberNavigationState(
        startKey = MyFinanceNavKey.Dashboard,
        topLevelKeys = TOP_LEVEL_NAV_KEYS
    )

    return remember(
        navigationState,
        coroutineScope,
    ) {
        MyFinanceAppState(
            navigationState = navigationState,
            coroutineScope = coroutineScope,
        )
    }
}

@Stable
class MyFinanceAppState(
    val navigationState: NavigationState,
    @Suppress("unused")
    val coroutineScope: CoroutineScope,
) {
    // We can add networkMonitor or other app-wide state here later if needed
}
