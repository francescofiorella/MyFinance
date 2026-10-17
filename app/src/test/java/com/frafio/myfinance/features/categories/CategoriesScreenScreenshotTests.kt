package com.frafio.myfinance.features.categories

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class CategoriesScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun categories() {
        composeTestRule.captureMultiDevice("CategoriesScreen") { Categories(sampleCategoriesUiState(screenshotToday)) }
    }

    @Test
    fun categories_dark() {
        composeTestRule.capturePhoneDark("CategoriesScreen") { Categories(sampleCategoriesUiState(screenshotToday)) }
    }

    @Test
    fun categories_noSpending() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "CategoriesScreenNoSpending", deviceName = "phone") {
            Categories(
                CategoriesUiState(
                    month = screenshotToday.withDayOfMonth(1),
                    spending = buildCategorySpending(emptyList(), emptyList())
                )
            )
        }
    }

    @Composable
    private fun Categories(state: CategoriesUiState) {
        CategoriesContent(
            state = state,
            onBackClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onCurrentMonth = {},
            onCategoryClick = {}
        )
    }
}
