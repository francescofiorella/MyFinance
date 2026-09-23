package com.frafio.myfinance.features.expenses

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import com.frafio.myfinance.testing.screenshot.screenshotDateLabel
import com.frafio.myfinance.testing.screenshot.screenshotExpenses
import com.frafio.myfinance.testing.screenshot.screenshotExpensesMetadata
import com.frafio.myfinance.testing.screenshot.screenshotToday
import kotlinx.coroutines.flow.flowOf
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
class ExpensesScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun populated() {
        composeTestRule.captureMultiDevice("ExpensesScreenPopulated") { Expenses(screenshotExpenses, isEmpty = false) }
    }

    @Test
    fun populated_dark() {
        composeTestRule.capturePhoneDark("ExpensesScreenPopulated") { Expenses(screenshotExpenses, isEmpty = false) }
    }

    @Test
    fun filtered() {
        composeTestRule.captureMultiDevice("ExpensesScreenFiltered") {
            Expenses(
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
    fun empty() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ExpensesScreenEmpty", deviceName = "phone") {
            Expenses(emptyList(), isEmpty = true)
        }
    }

    @Test
    fun loading() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ExpensesScreenLoading", deviceName = "phone") {
            Expenses(emptyList(), isEmpty = null)
        }
    }

    @Composable
    private fun Expenses(
        expenses: List<Expense>,
        isEmpty: Boolean?,
        searchQuery: String = "",
        selectedCategories: List<Int> = emptyList(),
        selectedLabels: List<String> = emptyList(),
        dateRange: Pair<LocalDate, LocalDate>? = null,
    ) {
        ExpensesContent(
            expenses = expenses,
            totalFilteredExpenses = 46.5,
            isExpensesEmpty = isEmpty,
            searchQuery = searchQuery,
            selectedCategories = selectedCategories,
            selectedLabels = selectedLabels,
            dateRange = dateRange,
            scrollToIdFlow = flowOf(null),
            itemMetadata = screenshotExpensesMetadata,
            onSearchQueryChanged = {},
            onCategoryFilterChanged = {},
            onLabelFilterChanged = { _, _ -> },
            onDateFilterChanged = {},
            onLoadMore = {},
            onFilterClick = {},
            onItemLongClick = { _, _ -> },
            onCategoryClick = { _, _ -> },
            getDateLabel = ::screenshotDateLabel,
        )
    }
}
