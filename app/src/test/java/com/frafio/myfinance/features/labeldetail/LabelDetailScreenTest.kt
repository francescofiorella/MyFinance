package com.frafio.myfinance.features.labeldetail

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class LabelDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    // Japan 2025: 12 expenses for 2199.50 on 11 days, from the flight on 3 Feb to 19 Aug.
    private fun setScreen(state: LabelDetailUiState = LabelDetailUiState.Loaded(buildLabelDetail(sampleTripExpenses())!!)) {
        composeTestRule.setThemedContent {
            LabelDetailContent(
                label = "Japan 2025",
                state = state,
                onBackClick = { events += "back" }
            )
        }
    }

    @Test
    fun showsTheLabel_andItsDateRange() {
        setScreen()

        composeTestRule.onNodeWithText("Japan 2025").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 Feb – 19 Aug 2025").assertIsDisplayed()
    }

    @Test
    fun statsGrid_showsTheLifetimeStats() {
        setScreen()

        assertStat(string(R.string.total), doubleToPrice(2199.5))
        assertStat(string(R.string.expenses), "12")
        assertStat(string(R.string.active_days), "11")
        assertStat(string(R.string.active_days), "out of 198 days")
        assertStat(string(R.string.average_per_active_day), doubleToPrice(2199.5 / 11))
        assertStat(string(R.string.average_per_expense), doubleToPrice(2199.5 / 12))
        assertStat(string(R.string.biggest_day), doubleToPrice(920.0))
        assertStat(string(R.string.biggest_day), "3 Feb 2025")
    }

    // A stat card's value sits next to its label.
    private fun assertStat(label: String, value: String) {
        composeTestRule.onNode(hasText(value) and hasParent(hasAnyChild(hasText(label)))).assertIsDisplayed()
    }

    @Test
    fun timeline_showsTheTripDays_withTheStretchesBeforeCollapsed() {
        setScreen()

        composeTestRule.onNodeWithTag("label_detail_scroll").performScrollToNode(hasText(string(R.string.spending_over_time)))
        // Flight, hotel, pass and the eight trip days.
        composeTestRule.onNodeWithText("average over 11 days").assertExists()
        composeTestRule.onNodeWithContentDescription("2 months without expenses").assertExists()
        composeTestRule.onNodeWithContentDescription("3 months without expenses").assertExists()
        composeTestRule.onNodeWithContentDescription("3 weeks without expenses").assertExists()
        composeTestRule.onNodeWithText("3 wk").assertExists()
    }

    @Test
    fun timeline_ofALongRunningLabel_isMonthly() {
        setScreen(LabelDetailUiState.Loaded(buildLabelDetail(sampleLongRunningExpenses())!!))

        composeTestRule.onNodeWithTag("label_detail_scroll").performScrollToNode(hasText(string(R.string.spending_over_time)))
        composeTestRule.onNodeWithText("average over 14 months").assertExists()
    }

    @Test
    fun lists_showTheBiggestExpenses_andTheMostFrequent() {
        setScreen()

        val scroll = composeTestRule.onNodeWithTag("label_detail_scroll")
        scroll.performScrollToNode(hasText(string(R.string.biggest_expenses_overall)))
        scroll.performScrollToNode(hasText("Flight"))
        composeTestRule.onNodeWithText("Flight").assertIsDisplayed()
        scroll.performScrollToNode(hasText(string(R.string.frequent_expenses_title)))
        scroll.performScrollToNode(hasText("Ramen"))
        composeTestRule.onNodeWithText("Ramen").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 times").assertIsDisplayed()
    }

    @Test
    fun biggestExpenses_showOnlyTheOtherLabels() {
        val expenses = listOf(
            sampleTripExpenses().first().copy(name = "Dinner", price = 5000.0, labels = listOf("Japan 2025", "Work"))
        )
        setScreen(LabelDetailUiState.Loaded(buildLabelDetail(expenses)!!))

        composeTestRule.onNodeWithTag("label_detail_scroll").performScrollToNode(hasText("Work"))
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()
        // Only the top bar names the label itself.
        composeTestRule.onAllNodesWithText("Japan 2025").assertCountEquals(1)
    }

    @Test
    fun empty_showsNoExpenses() {
        setScreen(LabelDetailUiState.Empty)

        composeTestRule.onNodeWithText(string(R.string.no_expenses)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.total)).assertDoesNotExist()
    }

    @Test
    fun loading_showsNoStats() {
        setScreen(LabelDetailUiState.Loading)

        composeTestRule.onNodeWithText(string(R.string.total)).assertDoesNotExist()
        composeTestRule.onNodeWithText(string(R.string.no_expenses)).assertDoesNotExist()
    }

    @Test
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(events).containsExactly("back")
    }
}
