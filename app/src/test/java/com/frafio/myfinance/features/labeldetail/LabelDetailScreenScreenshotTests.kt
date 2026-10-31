package com.frafio.myfinance.features.labeldetail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
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
        composeTestRule.captureMultiDevice("LabelDetailScreen") { Detail("Japan 2025", sampleTripExpenses()) }
    }

    @Test
    fun trip_dark() {
        composeTestRule.capturePhoneDark("LabelDetailScreen") { Detail("Japan 2025", sampleTripExpenses()) }
    }

    @Test
    fun longRunning() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "LabelDetailScreenLongRunning", deviceName = "phone") {
            Detail("Home renovation", sampleLongRunningExpenses())
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
