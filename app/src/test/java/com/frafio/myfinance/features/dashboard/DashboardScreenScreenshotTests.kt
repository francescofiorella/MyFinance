package com.frafio.myfinance.features.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotBarChart
import com.frafio.myfinance.testing.screenshot.screenshotExpenses
import com.frafio.myfinance.testing.screenshot.screenshotToday
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class DashboardScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("DashboardScreenPopulated") { Dashboard(monthlyBudget = 500.0) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("DashboardScreenPopulated") { Dashboard(monthlyBudget = 500.0) }
    }

    @Test
    fun withoutBudget() {
        composeTestRule.captureMultiDevice("DashboardScreenNoBudget") { Dashboard(monthlyBudget = 0.0) }
    }

    @Composable
    private fun Dashboard(monthlyBudget: Double) {
        DashboardContent(
            today = screenshotToday,
            monthShown = true,
            thisMonthSum = 450.0,
            thisYearSum = 3350.0,
            monthlyBudget = monthlyBudget,
            todaySum = 45.0,
            balanceYear = 2024,
            incomesSum = 12000.0,
            expensesSum = 3350.0,
            barChartData = screenshotBarChart,
            pieExpenses = screenshotExpenses,
            pieDate = screenshotToday,
            monthlyShownInPie = true,
            scrollState = rememberScrollState(),
            onToggleMonthShown = {},
            onPreviousYear = {},
            onNextYear = {},
            onTodayAnnualBalance = {},
            onPreviousBarDate = {},
            onNextBarDate = {},
            onTodayBarDate = {},
            onSwitchPieData = {},
            onPreviousPieDate = {},
            onNextPieDate = {},
            onTodayPieData = {},
        )
    }
}
