package com.frafio.myfinance.features.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.screenshotNarrowChartBars
import com.frafio.myfinance.testing.screenshot.ScreenshotDashboard
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class DashboardScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("DashboardScreenPopulated", screenshotNarrowChartBars) { ScreenshotDashboard(monthlyBudget = 500.0) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("DashboardScreenPopulated", screenshotNarrowChartBars) { ScreenshotDashboard(monthlyBudget = 500.0) }
    }

    @Test
    fun withoutBudget() {
        composeTestRule.captureMultiDevice("DashboardScreenNoBudget", screenshotNarrowChartBars) { ScreenshotDashboard(monthlyBudget = 0.0) }
    }

    @Test
    fun shortHistory() {
        // Three tracked months on Auto's seven bars: older months have no data and no range buttons are offered.
        composeTestRule.captureForDevice(
            DefaultTestDevices.PHONE.spec, "DashboardScreenShortHistory", deviceName = "phone",
            accessibilitySuppressions = screenshotNarrowChartBars,
        ) {
            ScreenshotDashboard(monthlyBudget = 500.0, shownCount = 7, availableRanges = listOf(0), trackingStart = LocalDate.of(2024, 10, 1))
        }
    }
}
