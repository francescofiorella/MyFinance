package com.frafio.myfinance.ui.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

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

    val snackbarHostState = remember { SnackbarHostState() }

    NavigationTrackingSideEffect(navigationState)

    return remember(
        navigationState,
        coroutineScope,
        snackbarHostState
    ) {
        MyFinanceAppState(
            navigationState = navigationState,
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState
        )
    }
}

@Stable
class MyFinanceAppState(
    val navigationState: NavigationState,
    val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var showProgress by mutableStateOf(false)

    private val _reselectEvent = MutableSharedFlow<NavKey>(replay = 0)
    val reselectEvent: SharedFlow<NavKey> = _reselectEvent.asSharedFlow()

    fun onReselect(key: NavKey) {
        coroutineScope.launch {
            _reselectEvent.emit(key)
        }
    }

    fun showSnackBar(
        message: String,
        actionText: String? = null,
        actionFun: () -> Unit = {},
        dismissFun: () -> Unit = {}
    ) {
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionText,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                actionFun()
            } else {
                dismissFun()
            }
        }
    }
}

/**
 * Stores information about navigation events for analytics or performance monitoring.
 */
@Composable
private fun NavigationTrackingSideEffect(navigationState: NavigationState) {
    LaunchedEffect(navigationState.currentKey) {
        // Track navigation events here (e.g. Firebase Analytics, JankStats)
        // Log.d("Navigation", "Navigated to: ${navigationState.currentKey}")
    }
}

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState not initialized")
}
