package com.frafio.myfinance.features.managelabels

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotLabels
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class ManageLabelsScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("ManageLabelsScreenPopulated") { ManageLabels(screenshotLabels) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("ManageLabelsScreenPopulated") { ManageLabels(screenshotLabels) }
    }

    @Test
    fun empty() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ManageLabelsScreenEmpty", deviceName = "phone") { ManageLabels(emptyList()) }
    }

    @androidx.compose.runtime.Composable
    private fun ManageLabels(labels: List<String>) {
        ManageLabelsContent(
            allLabels = labels,
            onBackClick = {},
            onAddLabel = {},
            onDeleteLabel = {},
            onEditLabel = { _, _ -> },
            snackbarHost = {},
        )
    }
}
