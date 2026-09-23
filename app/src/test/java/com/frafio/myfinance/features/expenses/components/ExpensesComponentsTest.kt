package com.frafio.myfinance.features.expenses.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.core.utils.getCategoryName
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/** `FilterChipBar`, `FilterExpensesSheet`, `CategorySheet` and `LabelsSheet`. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ExpensesComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()
    private val today = LocalDate.of(2024, 5, 29)

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    // region FilterChipBar

    private fun setChipBar(categories: List<Int>, labels: List<String>, date: Pair<LocalDate, LocalDate>?) {
        composeTestRule.setThemedContent {
            FilterChipBar(
                categories = categories, labels = labels, dateFilter = date,
                getDateLabel = { _, _ -> "Last 7 days" },
                onCategoryRemoved = { events += "category:$it" },
                onLabelRemoved = { events += "label:$it" },
                onDateRemoved = { events += "date" },
            )
        }
    }

    @Test
    fun chipBar_rendersLabelsDateAndCategories_andRemovesEach() {
        setChipBar(listOf(CATEGORIES.HOUSING.value), listOf("Dinner"), today.minusDays(7) to today)

        composeTestRule.onNodeWithText("Dinner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last 7 days").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(getCategoryName(CATEGORIES.HOUSING.value))).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(string(R.string.remove_item, "Dinner")).performClick()
        composeTestRule.onNodeWithContentDescription(string(R.string.remove_item, "Last 7 days")).performClick()
        composeTestRule.onNodeWithContentDescription(string(R.string.remove_item, string(getCategoryName(CATEGORIES.HOUSING.value)))).performClick()

        assertThat(events).containsExactly("label:Dinner", "date", "category:${CATEGORIES.HOUSING.value}").inOrder()
    }

    @Test
    fun chipBar_withoutDate_showsNoDateChip() {
        setChipBar(emptyList(), listOf("Dinner"), null)

        composeTestRule.onNodeWithText("Last 7 days").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(string(R.string.remove_item, "Dinner")).performClick()
        assertThat(events).containsExactly("label:Dinner")
    }

    // endregion

    // region FilterExpensesSheet

    private fun setFilterSheet(categoryEnabled: Boolean = true, labelEnabled: Boolean = true, dateRangeEnabled: Boolean = true) {
        composeTestRule.setThemedContent(inline = true) {
            FilterExpensesSheet(
                show = true, onDismiss = { events += "dismiss" },
                categoryEnabled = categoryEnabled, labelEnabled = labelEnabled, dateRangeEnabled = dateRangeEnabled,
                onSelectCategory = { events += "category" }, onSelectLabel = { events += "label" }, onSelectDateRange = { events += "date" },
            )
        }
    }

    @Test
    fun filterSheet_itemsSelectThenDismiss() {
        setFilterSheet()

        composeTestRule.onNodeWithTag("filter_label").performClick()
        composeTestRule.onNodeWithTag("filter_category").performClick()
        composeTestRule.onNodeWithTag("filter_date_range").performClick()

        assertThat(events).containsExactly("label", "dismiss", "category", "dismiss", "date", "dismiss").inOrder()
    }

    @Test
    fun filterSheet_disabledItems_ignoreClicks() {
        setFilterSheet(labelEnabled = false, dateRangeEnabled = false)

        composeTestRule.onNodeWithTag("filter_label").assertIsNotEnabled().performClick()
        composeTestRule.onNodeWithTag("filter_date_range").assertIsNotEnabled().performClick()
        composeTestRule.onNodeWithTag("filter_category").assertIsEnabled()

        assertThat(events).isEmpty()
    }

    // endregion

    // region CategorySheet

    @Test
    fun categorySheet_listsEveryCategory_andSelectsThenDismisses() {
        composeTestRule.setThemedContent(inline = true) {
            CategorySheet(show = true, onDismiss = { events += "dismiss" }, onCategorySelected = { events += "selected:$it" })
        }

        CATEGORIES.entries.filter { it.value in 0..8 }.forEach {
            composeTestRule.onNodeWithText(string(getCategoryName(it.value))).assertIsDisplayed()
        }
        composeTestRule.onNodeWithText(string(R.string.category)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(getCategoryName(CATEGORIES.GROCERIES.value))).performClick()

        assertThat(events).containsExactly("selected:${CATEGORIES.GROCERIES.value}", "dismiss").inOrder()
    }

    @Test
    fun categorySheet_disabledCategories_areNotEnabled() {
        composeTestRule.setThemedContent(inline = true) {
            CategorySheet(show = true, onDismiss = {}, onCategorySelected = { events += "selected:$it" }, disabledCategories = listOf(CATEGORIES.HOUSING.value))
        }

        composeTestRule.onNodeWithText(string(getCategoryName(CATEGORIES.HOUSING.value))).assertIsNotEnabled().performClick()
        composeTestRule.onNodeWithText(string(getCategoryName(CATEGORIES.DINING.value))).assertIsEnabled()

        assertThat(events).isEmpty()
    }

    @Test
    fun categorySheet_withExpense_showsItsHeader() {
        composeTestRule.setThemedContent(inline = true) {
            CategorySheet(show = true, onDismiss = {}, onCategorySelected = {}, expense = testExpense(name = "Pizza", price = 8.5, date = today))
        }

        composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
        composeTestRule.onNodeWithText("29/05/2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 8.50").assertIsDisplayed()
    }

    // endregion

    // region LabelsSheet

    @Test
    fun labelsSheet_togglesReportAndKeepTheirState() {
        composeTestRule.setThemedContent(inline = true) {
            LabelsSheet(show = true, onDismiss = {}, labels = listOf("Travel", "Dinner"), selectedLabels = listOf("Dinner"), onLabelCheckedChanged = { l, c -> events += "$l:$c" })
        }

        composeTestRule.onNodeWithText(string(R.string.labels)).assertIsDisplayed()
        val toggles = composeTestRule.onAllNodes(isToggleable())
        toggles[0].assertIsOff()
        toggles[1].assertIsOn()

        composeTestRule.onNodeWithText("Travel").performClick()
        toggles[0].assertIsOn()
        composeTestRule.onNodeWithText("Travel").performClick()
        toggles[0].assertIsOff()

        assertThat(events).containsExactly("Travel:true", "Travel:false").inOrder()
    }

    @Test
    fun labelsSheet_withExpense_selectsItsLabels_overSelectedLabels() {
        composeTestRule.setThemedContent(inline = true) {
            LabelsSheet(
                show = true, onDismiss = {}, labels = listOf("Travel", "Dinner"), selectedLabels = listOf("Dinner"),
                expense = testExpense(name = "Pizza", labels = listOf("Travel")), onLabelCheckedChanged = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
        val toggles = composeTestRule.onAllNodes(isToggleable())
        toggles[0].assertIsOn()
        toggles[1].assertIsOff()
    }

    // endregion
}
