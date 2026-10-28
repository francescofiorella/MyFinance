package com.frafio.myfinance.app

import androidx.activity.ComponentActivity
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.window.core.layout.WindowSizeClass
import com.frafio.myfinance.core.navigation.HomeTabKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.features.home.HomeScreenContent
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.ScreenshotBudget
import com.frafio.myfinance.testing.screenshot.ScreenshotDashboard
import com.frafio.myfinance.testing.screenshot.ScreenshotExpenses
import com.frafio.myfinance.testing.screenshot.ScreenshotProfile
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.screenshotExpenses
import com.frafio.myfinance.testing.screenshot.screenshotIncomes
import com.frafio.myfinance.testing.screenshot.screenshotNarrowChartBars
import com.frafio.myfinance.testing.screenshot.NoAccessibilitySuppressions
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityViewCheckResult
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

/** The README's screenshots: each tab inside the app shell, cut to the phone's screen rather than the whole page. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class ReadmeScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dashboard() = readme("ReadmeDashboard", HomeTabKey.Dashboard, screenshotNarrowChartBars) {
        ScreenshotDashboard(monthlyBudget = 500.0)
    }

    @Test
    fun expenses() = readme("ReadmeExpenses", HomeTabKey.Expenses) { ScreenshotExpenses(screenshotExpenses, isEmpty = false) }

    @Test
    fun budget() = readme("ReadmeBudget", HomeTabKey.Budget) {
        ScreenshotBudget(screenshotIncomes, isEmpty = false, monthlyBudget = 1200.0)
    }

    @Test
    fun profile() = readme("ReadmeProfile", HomeTabKey.Profile) {
        ScreenshotProfile(testUser(fullName = "Ada Lovelace", hasPassword = true))
    }

    private fun readme(
        name: String,
        tab: HomeTabKey,
        accessibilitySuppressions: Matcher<in AccessibilityViewCheckResult> = NoAccessibilitySuppressions,
        screen: @Composable () -> Unit,
    ) {
        val phone = DefaultTestDevices.PHONE.spec
        composeTestRule.captureForDevice(
            phone, name, deviceName = "phone", fullPage = false, accessibilitySuppressions = accessibilitySuppressions,
        ) {
            HomeScreenContent(
                appState = rememberMyFinanceAppState(),
                currentTab = tab,
                profilePicture = null,
                proPicChoice = null,
                windowAdaptiveInfo = WindowAdaptiveInfo(WindowSizeClass(phone.widthDp, phone.heightDp), Posture()),
                onTabClick = {},
                onAddClick = {},
                onLogoutClick = {},
                onProPicClick = {},
                screenContent = screen,
            )
        }
    }
}
