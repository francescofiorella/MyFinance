package com.frafio.myfinance.core.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Centre text only: the arcs use `pointerInput` without semantics, so they cannot be addressed. */
@RunWith(RobolectricTestRunner::class)
class PieChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val items = listOf(
        PieChartItem(value = 30.0, label = "Dining", icon = R.drawable.ic_home_filled),
        PieChartItem(value = 45.0, label = "Groceries", icon = R.drawable.ic_shopping_cart_filled),
    )

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    @Test
    fun nothingSelected_showsTotalAndTheSum() {
        composeTestRule.setThemedContent {
            PieChart(items = items, animate = false)
        }

        composeTestRule.onNodeWithText(string(R.string.total)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 75").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dining").assertDoesNotExist()
    }

    @Test
    fun emptyItems_showsTotalAndZero() {
        composeTestRule.setThemedContent {
            PieChart(items = emptyList(), animate = false)
        }

        composeTestRule.onNodeWithText(string(R.string.total)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 0").assertIsDisplayed()
    }

    @Test
    fun animated_showsTheSameCentreText() {
        composeTestRule.setThemedContent {
            PieChart(items = items)
        }

        composeTestRule.onNodeWithText(string(R.string.total)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 75").assertIsDisplayed()
    }
}
