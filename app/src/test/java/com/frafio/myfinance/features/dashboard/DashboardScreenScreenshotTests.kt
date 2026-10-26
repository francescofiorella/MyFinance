package com.frafio.myfinance.features.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.charts.R as ChartsR
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotBarChart
import com.frafio.myfinance.testing.screenshot.screenshotExpenses
import com.frafio.myfinance.testing.screenshot.screenshotToday
import com.frafio.myfinance.testing.util.string
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils.matchesElements
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck
import com.google.android.apps.common.testing.accessibility.framework.matcher.ElementMatchers.withContentDescription
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class DashboardScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    // Twelve month bars share a phone-width card, about 30 dp each: 48 dp needs a chart redesign.
    private val narrowChartBars = Matchers.allOf(
        matchesCheck(TouchTargetSizeCheck::class.java),
        matchesElements(
            withContentDescription(
                Matchers.isIn(
                    screenshotBarChart.map {
                        string(
                            ChartsR.string.charts_item_description,
                            YearMonth.of(it.year, it.month).format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            doubleToPrice(it.value),
                        )
                    },
                ),
            ),
        ),
    )

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("DashboardScreenPopulated", narrowChartBars) { Dashboard(monthlyBudget = 500.0) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("DashboardScreenPopulated", narrowChartBars) { Dashboard(monthlyBudget = 500.0) }
    }

    @Test
    fun withoutBudget() {
        composeTestRule.captureMultiDevice("DashboardScreenNoBudget", narrowChartBars) { Dashboard(monthlyBudget = 0.0) }
    }

    @Test
    fun shortHistory() {
        // Three tracked months on Auto's seven bars: older months have no data and no range buttons are offered.
        composeTestRule.captureForDevice(
            DefaultTestDevices.PHONE.spec, "DashboardScreenShortHistory", deviceName = "phone",
            accessibilitySuppressions = narrowChartBars,
        ) {
            Dashboard(monthlyBudget = 500.0, shownCount = 7, availableRanges = listOf(0), trackingStart = LocalDate.of(2024, 10, 1))
        }
    }

    @Composable
    private fun Dashboard(
        monthlyBudget: Double,
        shownCount: Int = screenshotBarChart.size,
        availableRanges: List<Int> = ALL_MONTH_RANGES,
        trackingStart: LocalDate? = null
    ) {
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
            barChartShownCount = shownCount,
            barChartAvailableRanges = availableRanges,
            barChartTrackingStart = trackingStart,
            onSwitchPieData = {},
            onPreviousPieDate = {},
            onNextPieDate = {},
            onTodayPieData = {},
        )
    }
}
