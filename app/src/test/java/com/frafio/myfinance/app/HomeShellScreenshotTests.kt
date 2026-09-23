package com.frafio.myfinance.app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.window.core.layout.WindowSizeClass
import com.frafio.myfinance.core.navigation.HomeTabKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.features.home.HomeScreenContent
import com.frafio.myfinance.testing.screenshot.DeviceSpec
import com.frafio.myfinance.testing.screenshot.captureForDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

/** The app shell (navigation bar vs rail, top bar, FAB) at the three width classes. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class HomeShellScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun compactWidth_showsNavigationBar() = shell("compactWidth_showsNavigationBar", 411, 891)

    @Test
    fun mediumWidth_showsNavigationRail() = shell("mediumWidth_showsNavigationRail", 700, 891)

    @Test
    fun expandedWidth_showsNavigationRail() = shell("expandedWidth_showsNavigationRail", 1280, 800)

    @Test
    fun profileTab_showsLogoutAction() = shell("profileTab_showsLogoutAction", 411, 891, tab = HomeTabKey.Profile)

    private fun shell(name: String, widthDp: Int, heightDp: Int, tab: HomeTabKey = HomeTabKey.Dashboard) {
        // The device size is the layout size: `ForcedSize` only rescales density, it does not constrain.
        composeTestRule.captureForDevice(DeviceSpec(widthDp, heightDp), name, deviceName = "shell") {
            HomeShell(widthDp, heightDp, tab)
        }
    }

    @Composable
    private fun HomeShell(widthDp: Int, heightDp: Int, tab: HomeTabKey) {
        HomeScreenContent(
            appState = rememberMyFinanceAppState(),
            currentTab = tab,
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
}
