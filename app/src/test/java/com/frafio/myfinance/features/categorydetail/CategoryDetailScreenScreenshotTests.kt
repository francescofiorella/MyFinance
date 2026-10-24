package com.frafio.myfinance.features.categorydetail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class CategoryDetailScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val month = screenshotToday.withDayOfMonth(1)
    private val stats = sampleCategoryDetailStats(month)

    // Twelve month bars share a phone-width card, as on the dashboard: the same known exception.
    private val narrowChartBars = Matchers.allOf(
        matchesCheck(TouchTargetSizeCheck::class.java),
        matchesElements(
            withContentDescription(
                Matchers.isIn(
                    stats.trend.map {
                        string(
                            R.string.charts_item_description,
                            YearMonth.of(it.year, it.month).format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            doubleToPrice(it.value),
                        )
                    },
                ),
            ),
        ),
    )

    @Test
    fun detail() {
        composeTestRule.captureMultiDevice("CategoryDetailScreen", narrowChartBars) { Detail(stats) }
    }

    @Test
    fun detail_dark() {
        composeTestRule.capturePhoneDark("CategoryDetailScreen", narrowChartBars) { Detail(stats) }
    }

    @Test
    fun detail_loading() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "CategoryDetailScreenLoading", deviceName = "phone") {
            Detail(null)
        }
    }

    @Composable
    private fun Detail(stats: CategoryDetailStats?) {
        CategoryDetailContent(
            category = ExpenseCategories.get(CATEGORIES.DINING.value),
            month = month,
            stats = stats,
            onBackClick = {},
            onSeeAllExpenses = {},
            onFrequentExpenseClick = {}
        )
    }
}
