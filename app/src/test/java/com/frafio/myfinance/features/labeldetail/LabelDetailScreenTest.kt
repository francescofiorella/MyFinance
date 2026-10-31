package com.frafio.myfinance.features.labeldetail

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
                onBackClick = { events += "back" },
                onSeeAllExpenses = { events += "seeAll" },
                onFrequentExpenseClick = { events += "frequent:${it.name}" }
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
        assertStat(string(R.string.average), doubleToPrice(2199.5 / 12))
        assertStat(doubleToPrice(2199.5 / 12), "out of 12 expenses")
        assertStat(string(R.string.average), doubleToPrice(2199.5 / 11))
        assertStat(doubleToPrice(2199.5 / 11), "out of 11 active days")
    }

    // [value] sits in the same stat card as [label].
    private fun assertStat(label: String, value: String) {
        composeTestRule.onNode(hasText(value) and hasParent(hasAnyChild(hasText(label)))).assertIsDisplayed()
    }

    @Test
    fun categories_rowsShowAmountAndShare_highestFirst() {
        setScreen()

        val scroll = composeTestRule.onNodeWithTag("label_detail_scroll")
        scroll.performScrollToNode(hasTestTag("label_category_item"))
        composeTestRule.onAllNodesWithTag("label_category_item").assertCountEquals(5)
        composeTestRule.onAllNodesWithTag("label_category_item", useUnmergedTree = true)[0].assert(hasAnyDescendant(hasText(string(R.string.transportation))))
        scroll.performScrollToNode(hasText("${doubleToPrice(1387.0)} · 63%"))
        composeTestRule.onNodeWithText("${doubleToPrice(1387.0)} · 63%").assertIsDisplayed()
        composeTestRule.onNodeWithText("${doubleToPrice(540.0)} · 25%").assertExists()
    }

    @Test
    fun tappingACategoryRow_selectsItsSlice_andTappingAgainClearsIt() {
        setScreen()
        val housing = string(R.string.housing)
        val row = composeTestRule.onAllNodesWithTag("label_category_item")[1]
        composeTestRule.onNodeWithTag("label_detail_scroll").performScrollToNode(hasText(housing))
        composeTestRule.onAllNodesWithText(housing).assertCountEquals(1)

        row.performClick()
        // The pie's centre names the selected category too.
        composeTestRule.onAllNodesWithText(housing).assertCountEquals(2)

        row.performClick()
        composeTestRule.onAllNodesWithText(housing).assertCountEquals(1)
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
    fun tappingAMostFrequentRow_reportsThatExpense() {
        setScreen()

        // Most used first: Ramen.
        composeTestRule.onAllNodesWithTag("frequent_expense_item")[0].performScrollTo().performClick()

        assertThat(events).containsExactly("frequent:Ramen")
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
    fun seeAllExpenses_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithTag("label_see_all").performScrollTo().performClick()

        assertThat(events).containsExactly("seeAll")
    }

    @Test
    fun seeAllExpenses_isHiddenWithoutExpenses() {
        setScreen(LabelDetailUiState.Empty)

        composeTestRule.onNodeWithTag("label_see_all").assertDoesNotExist()
    }

    @Test
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(events).containsExactly("back")
    }
}
