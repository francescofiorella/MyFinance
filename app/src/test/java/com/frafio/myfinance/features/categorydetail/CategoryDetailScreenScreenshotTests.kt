package com.frafio.myfinance.features.categorydetail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotToday
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
class CategoryDetailScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val month = screenshotToday.withDayOfMonth(1)
    private val stats = sampleCategoryDetailStats(month)

    @Test
    fun detail() {
        composeTestRule.captureMultiDevice("CategoryDetailScreen") { Detail(stats) }
    }

    @Test
    fun detail_dark() {
        composeTestRule.capturePhoneDark("CategoryDetailScreen") { Detail(stats) }
    }

    @Test
    fun detail_loading() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "CategoryDetailScreenLoading", deviceName = "phone") {
            Detail(null)
        }
    }

    @Test
    fun detail_shortHistory() {
        // Three tracked months on Auto's seven points: older months have no data and no range buttons are offered.
        composeTestRule.captureForDevice(
            DefaultTestDevices.PHONE.spec, "CategoryDetailScreenShortHistory", deviceName = "phone"
        ) {
            Detail(stats, shownCount = 7, availableRanges = listOf(0), trackingStart = month.minusMonths(2))
        }
    }

    @Composable
    private fun Detail(
        stats: CategoryDetailStats?,
        shownCount: Int = stats?.trend.orEmpty().size,
        availableRanges: List<Int> = ALL_MONTH_RANGES,
        trackingStart: LocalDate? = null
    ) {
        CategoryDetailContent(
            category = ExpenseCategories.get(CATEGORIES.DINING.value),
            month = month,
            stats = stats,
            onBackClick = {},
            onSeeAllExpenses = {},
            onFrequentExpenseClick = {},
            trendShownCount = shownCount,
            trendAvailableRanges = availableRanges,
            trendTrackingStart = trackingStart
        )
    }
}
