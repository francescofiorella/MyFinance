package com.frafio.myfinance.core.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Centre text; what the arcs expose to accessibility services is in `app/AccessibilityTest`. */
@RunWith(RobolectricTestRunner::class)
class PieChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val items
        @Composable get() = listOf(
            categoryPieChartItem(value = 30.0, label = "Dining", icon = R.drawable.ic_home_filled, colors = ExpenseCategories.all[0].colors),
            categoryPieChartItem(value = 45.0, label = "Groceries", icon = R.drawable.ic_shopping_cart_filled, colors = ExpenseCategories.all[1].colors),
        )

    @Composable
    private fun TestPieChart(items: List<PieChartItem>, animationSpec: AnimationSpec<Float> = PieChartDefaults.AnimationSpec) =
        PieChart(
            items = items,
            totalLabel = string(R.string.total),
            valueFormatter = ::doubleToPriceWithoutDecimals,
            animationSpec = animationSpec
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
            TestPieChart(items = items, animationSpec = snap())
        }

        composeTestRule.onNodeWithText(string(R.string.total)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 75").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dining").assertDoesNotExist()
    }

    @Test
    fun emptyItems_showsTotalAndZero() {
        composeTestRule.setThemedContent {
            TestPieChart(items = emptyList(), animationSpec = snap())
        }

        composeTestRule.onNodeWithText(string(R.string.total)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 0").assertIsDisplayed()
    }

    @Test
    fun animated_showsTheSameCentreText() {
        composeTestRule.setThemedContent {
            TestPieChart(items = items)
        }

        composeTestRule.onNodeWithText(string(R.string.total)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 75").assertIsDisplayed()
    }
}
