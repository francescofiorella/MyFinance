package com.frafio.myfinance.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MyFinanceAppStateTest {

    private val snackbarHostState = SnackbarHostState()

    private val navigationState = NavigationState(
        startKey = HomeTabKey.Dashboard,
        topLevelStack = NavBackStack(HomeTabKey.Dashboard),
        subStacks = listOf(HomeTabKey.Dashboard, HomeTabKey.Expenses, HomeTabKey.Budget, HomeTabKey.Profile)
            .associateWith { key -> NavBackStack(key) },
    )

    // A shown snackbar keeps its coroutine suspended; backgroundScope is cancelled when the test ends.
    private fun TestScope.appState() = MyFinanceAppState(
        coroutineScope = backgroundScope,
        snackbarHostState = snackbarHostState,
        navigationState = navigationState,
    )

    @Test
    fun showProgress_defaultsToFalse() = runTest {
        assertThat(appState().showProgress).isFalse()
    }

    @Test
    fun navigationState_isTheOnePassedIn() = runTest {
        assertThat(appState().navigationState).isSameInstanceAs(navigationState)
    }

    @Test
    fun onReselect_emitsToSubscribers() = runTest {
        val appState = appState()
        val reselects = mutableListOf<NavKey>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { appState.reselectEvent.toList(reselects) }

        appState.onReselect(HomeTabKey.Expenses)
        runCurrent()

        assertThat(reselects).containsExactly(HomeTabKey.Expenses)
    }

    @Test
    fun onReselect_withoutASubscriber_isDropped() = runTest {
        val appState = appState()

        appState.onReselect(HomeTabKey.Expenses)
        runCurrent()

        val reselects = mutableListOf<NavKey>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { appState.reselectEvent.toList(reselects) }
        runCurrent()

        assertThat(reselects).isEmpty()
    }

    @Test
    fun showSnackBar_showsTheMessage() = runTest {
        val appState = appState()

        appState.showSnackBar("saved")
        runCurrent()

        val visuals = snackbarHostState.currentSnackbarData?.visuals
        assertThat(visuals?.message).isEqualTo("saved")
        assertThat(visuals?.actionLabel).isNull()
    }

    @Test
    fun showSnackBar_actionInvokesActionFun() = runTest {
        val appState = appState()
        var actions = 0
        var dismissals = 0

        appState.showSnackBar("x", actionText = "Undo", actionFun = { actions++ }, dismissFun = { dismissals++ })
        runCurrent()
        assertThat(snackbarHostState.currentSnackbarData?.visuals?.actionLabel).isEqualTo("Undo")

        snackbarHostState.currentSnackbarData!!.performAction()
        runCurrent()

        assertThat(actions).isEqualTo(1)
        assertThat(dismissals).isEqualTo(0)
        assertThat(snackbarHostState.currentSnackbarData).isNull()
    }

    @Test
    fun showSnackBar_dismissInvokesDismissFun() = runTest {
        val appState = appState()
        var actions = 0
        var dismissals = 0

        appState.showSnackBar("x", actionFun = { actions++ }, dismissFun = { dismissals++ })
        runCurrent()

        snackbarHostState.currentSnackbarData!!.dismiss()
        runCurrent()

        assertThat(actions).isEqualTo(0)
        assertThat(dismissals).isEqualTo(1)
        assertThat(snackbarHostState.currentSnackbarData).isNull()
    }

    @Test
    fun showSnackBar_replacesTheCurrentSnackbar() = runTest {
        val appState = appState()
        var firstDismissed = false

        appState.showSnackBar("first", dismissFun = { firstDismissed = true })
        runCurrent()
        assertThat(snackbarHostState.currentSnackbarData?.visuals?.message).isEqualTo("first")

        appState.showSnackBar("second")
        runCurrent()

        assertThat(firstDismissed).isTrue()
        assertThat(snackbarHostState.currentSnackbarData?.visuals?.message).isEqualTo("second")
    }
}
