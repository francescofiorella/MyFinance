package com.frafio.myfinance.features.labeldetail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.charts.R as ChartsR
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
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

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class LabelDetailScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun trip() {
        composeTestRule.captureMultiDevice("LabelDetailScreen", overlappingTripPieIcons) {
            Detail("Japan 2025", sampleTripExpenses())
        }
    }

    @Test
    fun trip_dark() {
        composeTestRule.capturePhoneDark("LabelDetailScreen", overlappingTripPieIcons) {
            Detail("Japan 2025", sampleTripExpenses())
        }
    }

    @Test
    fun longRunning() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "LabelDetailScreenLongRunning", deviceName = "phone") {
            Detail("Home renovation", sampleLongRunningExpenses())
        }
    }

    // Entertainment (1%) sits right before Dining (7%): their icons overlap and narrow each other's target.
    private val overlappingTripPieIcons
        get() = Matchers.allOf(
            matchesCheck(TouchTargetSizeCheck::class.java),
            matchesElements(
                withContentDescription(
                    Matchers.isIn(
                        listOf(
                            string(ChartsR.string.charts_item_description, string(R.string.dining), doubleToPriceWithoutDecimals(159.5)),
                            string(ChartsR.string.charts_item_description, string(R.string.entertainment), doubleToPriceWithoutDecimals(28.0)),
                        ),
                    ),
                ),
            ),
        )

    @Composable
    private fun Detail(label: String, expenses: List<Expense>) {
        LabelDetailContent(
            label = label,
            state = LabelDetailUiState.Loaded(buildLabelDetail(expenses)!!),
            onBackClick = {},
            onSeeAllExpenses = {},
            onFrequentExpenseClick = {},
            onCategoryClick = {}
        )
    }
}
