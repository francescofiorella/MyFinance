package com.frafio.myfinance.features.labeldetail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.charts.R as ChartsR
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
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
import java.time.format.DateTimeFormatter

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class LabelDetailScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun trip() {
        composeTestRule.captureMultiDevice("LabelDetailScreen", narrowTimelineBars(sampleTripExpenses())) {
            Detail("Japan 2025", sampleTripExpenses())
        }
    }

    @Test
    fun trip_dark() {
        composeTestRule.capturePhoneDark("LabelDetailScreen", narrowTimelineBars(sampleTripExpenses())) {
            Detail("Japan 2025", sampleTripExpenses())
        }
    }

    @Test
    fun longRunning() {
        composeTestRule.captureForDevice(
            DefaultTestDevices.PHONE.spec, "LabelDetailScreenLongRunning", deviceName = "phone",
            accessibilitySuppressions = narrowTimelineBars(sampleLongRunningExpenses()),
        ) {
            Detail("Home renovation", sampleLongRunningExpenses())
        }
    }

    // Up to a month of bars share a phone-width card, 10 to 25 dp each: 48 dp needs a chart redesign.
    private fun narrowTimelineBars(expenses: List<Expense>) = Matchers.allOf(
        matchesCheck(TouchTargetSizeCheck::class.java),
        matchesElements(withContentDescription(Matchers.isIn(timelineBarDescriptions(expenses)))),
    )

    private fun timelineBarDescriptions(expenses: List<Expense>): List<String> {
        val timeline = buildLabelTimeline(expenses)
        val pattern = when (timeline.unit) {
            TimelineUnit.DAY -> "d MMMM yyyy"
            TimelineUnit.MONTH -> "MMMM yyyy"
            else -> error("The samples are daily or monthly.")
        }
        return timeline.slots.filterIsInstance<TimelineSlot.Bucket>().map {
            string(ChartsR.string.charts_item_description, it.start.format(DateTimeFormatter.ofPattern(pattern)), doubleToPrice(it.total))
        }
    }

    @Composable
    private fun Detail(label: String, expenses: List<Expense>) {
        LabelDetailContent(
            label = label,
            state = LabelDetailUiState.Loaded(buildLabelDetail(expenses)!!),
            onBackClick = {}
        )
    }
}
