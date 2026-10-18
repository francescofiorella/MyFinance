package com.frafio.myfinance.features.managecategories

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.data.model.ExpenseCategories
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
class ManageCategoriesScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun manageCategories() {
        composeTestRule.captureMultiDevice("ManageCategoriesScreen") {
            ManageCategoriesContent(categories = ExpenseCategories.all, onBackClick = {})
        }
    }

    @Test
    fun manageCategories_dark() {
        composeTestRule.capturePhoneDark("ManageCategoriesScreen") {
            ManageCategoriesContent(categories = ExpenseCategories.all, onBackClick = {})
        }
    }
}
