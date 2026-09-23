package com.frafio.myfinance.app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.window.core.layout.WindowSizeClass
import com.frafio.myfinance.core.navigation.HomeTabKey
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.features.home.HomeScreenContent
import com.frafio.myfinance.testing.screenshot.DeviceSpec
import com.frafio.myfinance.testing.screenshot.captureAfter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

/**
 * Where the snackbar lands in the Home shell: above the FAB, clear of the navigation bar at compact
 * width and of the rail at expanded width. `MyFinanceAppState.showSnackBar` is bypassed because it
 * hard-codes `SnackbarDuration.Short`, which would dismiss itself before the capture.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1400dp-h1000dp-420dpi")
@LooperMode(LooperMode.Mode.PAUSED)
class SnackbarScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun snackbar(name: String, widthDp: Int, heightDp: Int, darkMode: Boolean = false, showSnackbar: Boolean = true) {
        lateinit var appState: MyFinanceAppState
        composeTestRule.captureAfter(
            // The device size is the layout size: `ForcedSize` only rescales density, it does not constrain.
            device = DeviceSpec(widthDp, heightDp),
            screenshotName = name,
            deviceName = if (darkMode) "shell_dark" else "shell",
            darkMode = darkMode,
            action = {
                if (showSnackbar) {
                    appState.snackbarHostState.showSnackbar(
                        message = "Expense deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Indefinite,
                    )
                }
            },
        ) {
            appState = rememberMyFinanceAppState()
            HomeShell(appState, widthDp, heightDp)
        }
    }

    @Composable
    private fun HomeShell(appState: MyFinanceAppState, widthDp: Int, heightDp: Int) {
        HomeScreenContent(
            appState = appState,
            currentTab = HomeTabKey.Expenses,
            profilePicture = null,
            proPicChoice = null,
            windowAdaptiveInfo = WindowAdaptiveInfo(WindowSizeClass(widthDp, heightDp), Posture()),
            onTabClick = {},
            onAddClick = {},
            onLogoutClick = {},
            onProPicClick = {},
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Screen content") }
        }
    }

    @Test
    fun snackbar_compactWidth() = snackbar("snackbar_compact", 411, 891)

    @Test
    fun snackbar_compactWidth_dark() = snackbar("snackbar_compact", 411, 891, darkMode = true)

    @Test
    fun snackbar_compactWidth_withoutSnackbar() = snackbar("snackbar_compact_noSnackbar", 411, 891, showSnackbar = false)

    @Test
    fun snackbar_mediumWidth() = snackbar("snackbar_medium", 700, 891)

    @Test
    fun snackbar_mediumWidth_dark() = snackbar("snackbar_medium", 700, 891, darkMode = true)

    @Test
    fun snackbar_expandedWidth() = snackbar("snackbar_expanded", 1280, 800)

    @Test
    fun snackbar_expandedWidth_dark() = snackbar("snackbar_expanded", 1280, 800, darkMode = true)

    @Test
    fun snackbar_expandedWidth_withoutSnackbar() = snackbar("snackbar_expanded_noSnackbar", 1280, 800, showSnackbar = false)
}
