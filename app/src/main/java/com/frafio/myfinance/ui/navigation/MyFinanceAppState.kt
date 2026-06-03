package com.frafio.myfinance.ui.navigation

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
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

@Composable
fun rememberMyFinanceAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MyFinanceAppState {
    val snackbarHostState = remember { SnackbarHostState() }
    val navigationState = rememberNavigationState(
        startKey = HomeTabKey.Dashboard,
        topLevelKeys = setOf(
            HomeTabKey.Dashboard,
            HomeTabKey.Expenses,
            HomeTabKey.Budget,
            HomeTabKey.Profile
        )
    )

    return remember(
        coroutineScope,
        snackbarHostState,
        navigationState
    ) {
        MyFinanceAppState(
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState,
            navigationState = navigationState
        )
    }
}

@Stable
class MyFinanceAppState(
    val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState,
    val navigationState: NavigationState
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

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("SnackbarHostState not initialized")
}
