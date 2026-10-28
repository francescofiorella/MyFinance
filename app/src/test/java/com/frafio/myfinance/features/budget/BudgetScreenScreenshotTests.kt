package com.frafio.myfinance.features.budget

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.ScreenshotBudget
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotIncomes
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import com.frafio.myfinance.features.budget.components.EditBudgetSheet

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class BudgetScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editBudgetSheet() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "EditBudgetSheet", deviceName = "phone") {
            EditBudgetSheet(show = true, budget = 1200.0, onDismiss = {}, onEditBudget = {})
        }
    }

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("BudgetScreenPopulated") { ScreenshotBudget(screenshotIncomes, isEmpty = false, monthlyBudget = 1200.0) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("BudgetScreenPopulated") { ScreenshotBudget(screenshotIncomes, isEmpty = false, monthlyBudget = 1200.0) }
    }

    @Test
    fun empty() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "BudgetScreenEmpty", deviceName = "phone") {
            ScreenshotBudget(emptyList(), isEmpty = true, monthlyBudget = 0.0)
        }
    }
}
