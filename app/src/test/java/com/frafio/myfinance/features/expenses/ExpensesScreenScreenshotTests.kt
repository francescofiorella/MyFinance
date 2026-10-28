package com.frafio.myfinance.features.expenses

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.ScreenshotExpenses
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotExpenses
import com.frafio.myfinance.testing.screenshot.screenshotToday
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import com.frafio.myfinance.features.expenses.components.CategorySheet
import com.frafio.myfinance.features.expenses.components.FilterExpensesSheet
import com.frafio.myfinance.features.expenses.components.LabelsSheet
import com.frafio.myfinance.testing.screenshot.screenshotLabels

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class ExpensesScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun categorySheet() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "CategorySheet", deviceName = "phone") {
            CategorySheet(show = true, onDismiss = {}, onCategorySelected = {}, expense = screenshotExpenses[1])
        }
    }

    @Test
    fun filterExpensesSheet() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "FilterExpensesSheet", deviceName = "phone") {
            FilterExpensesSheet(
                show = true, onDismiss = {},
                categoryEnabled = true, labelEnabled = true, dateRangeEnabled = false,
                onSelectCategory = {}, onSelectLabel = {}, onSelectDateRange = {},
            )
        }
    }

    @Test
    fun labelsSheet() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "LabelsSheet", deviceName = "phone") {
            LabelsSheet(
                show = true, onDismiss = {}, expense = screenshotExpenses[1],
                labels = screenshotLabels, selectedLabels = screenshotExpenses[1].labels,
                onLabelCheckedChanged = { _, _ -> },
            )
        }
    }

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("ExpensesScreenPopulated") { ScreenshotExpenses(screenshotExpenses, isEmpty = false) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("ExpensesScreenPopulated") { ScreenshotExpenses(screenshotExpenses, isEmpty = false) }
    }

    @Test
    fun filtered() {
        composeTestRule.captureMultiDevice("ExpensesScreenFiltered") {
            ScreenshotExpenses(
                screenshotExpenses.take(3),
                isEmpty = false,
                searchQuery = "Pizza",
                selectedCategories = listOf(FirestoreEnums.CATEGORIES.DINING.value),
                selectedLabels = listOf("Dinner"),
                dateRange = screenshotToday.minusDays(2) to screenshotToday,
            )
        }
    }

    @Test
    fun searchExpanded() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ExpensesScreenSearchExpanded", deviceName = "phone") {
            ScreenshotExpenses(screenshotExpenses, isEmpty = false, searchQuery = "Pizza", isSearchExpanded = true)
        }
    }

    @Test
    fun empty() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ExpensesScreenEmpty", deviceName = "phone") {
            ScreenshotExpenses(emptyList(), isEmpty = true)
        }
    }

    @Test
    fun loading() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ExpensesScreenLoading", deviceName = "phone") {
            ScreenshotExpenses(emptyList(), isEmpty = null)
        }
    }
}
