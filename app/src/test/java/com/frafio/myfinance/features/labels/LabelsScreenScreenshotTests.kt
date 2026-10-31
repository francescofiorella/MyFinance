package com.frafio.myfinance.features.labels

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
class LabelsScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun labels() {
        composeTestRule.captureMultiDevice("LabelsScreen") { Labels(sampleLabelGroups()) }
    }

    @Test
    fun labels_dark() {
        composeTestRule.capturePhoneDark("LabelsScreen") { Labels(sampleLabelGroups()) }
    }

    @Test
    fun labels_empty() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "LabelsScreenEmpty", deviceName = "phone") {
            Labels(emptyList())
        }
    }

    @Composable
    private fun Labels(groups: List<LabelGroup>) {
        LabelsContent(
            groups = groups,
            onBackClick = {},
            onManageClick = {}
        )
    }
}
