package com.frafio.myfinance.features.frequentexpenses

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class FrequentExpensesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val to = LocalDate.of(2024, 5, 1)
    // Sushi Bar: € 128.00 and € 32.50 on 10 May, € 114.00 on 10 April, € 96.00 on 10 January.
    private val days = sampleFrequentExpenseDays(to)
    private var backClicks = 0

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    private fun setScreen() {
        composeTestRule.setThemedContent {
            FrequentExpensesContent(from = to.minusMonths(11), to = to, days = days, onBackClick = { backClicks++ })
        }
    }

    @Test
    fun title_showsMostFrequentAndThePeriod() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.most_frequent)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Jun 2023 – May 2024").assertIsDisplayed()
    }

    @Test
    fun eachDayHasADateHeader_withoutItsTotal() {
        setScreen()

        composeTestRule.onNodeWithText("10 May 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("10 Apr 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(128.0)).assertIsDisplayed()
        // 128.00 + 32.50 would be the total of 10 May.
        composeTestRule.onNodeWithText(doubleToPrice(160.5)).assertDoesNotExist()
    }

    @Test
    fun rows_areReadOnly() {
        setScreen()

        composeTestRule.onAllNodesWithTag("frequent_expense_row").assertCountEquals(4)
        // Only the back button can be clicked.
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(
            string(R.string.change_category, string(R.string.dining)),
            useUnmergedTree = true
        ).assertDoesNotExist()
    }

    @Test
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(backClicks).isEqualTo(1)
    }
}
