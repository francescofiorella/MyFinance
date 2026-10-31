package com.frafio.myfinance.features.labeldetail

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
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

        composeTestRule.onNodeWithText(doubleToPrice(2199.5)).assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
        composeTestRule.onNodeWithText("11").assertIsDisplayed()
        composeTestRule.onNodeWithText("out of 198 days").assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(2199.5 / 11)).assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(2199.5 / 12)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.biggest_day)).assertIsDisplayed()
        composeTestRule.onNodeWithText("3 Feb 2025").assertIsDisplayed()
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
