package com.frafio.myfinance.features.categories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class CategoriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    // Housing 450 (465 before), Dining 320 (285), Groceries 210 (228), Transportation 95 (95), Health 40 (new),
    // Miscellaneous 25 (new); the other three spent nothing.
    private fun setScreen(state: CategoriesUiState = sampleCategoriesUiState(LocalDate.of(2024, 5, 1))) {
        composeTestRule.setThemedContent {
            CategoriesContent(
                state = state,
                onBackClick = { events += "back" },
                onPreviousMonth = { events += "previous" },
                onNextMonth = { events += "next" },
                onCurrentMonth = { events += "current" },
                onCategoryClick = { events += "category:$it" }
            )
        }
    }

    @Test
    fun showsTheMonthAndItsTotal() {
        setScreen()

        composeTestRule.onNodeWithText("May 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(1140.0)).assertIsDisplayed()
    }

    @Test
    fun rows_showAmountAndShare_andCategoriesWithNoSpendingAreListedApart() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.housing)).assertIsDisplayed()
        composeTestRule.onNodeWithText("${doubleToPrice(450.0)} · 39%").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.no_spending_this_month)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.education)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun changeChips_describeTheChangeFromLastMonth() {
        setScreen()

        // 450 vs 465 is -3.2%; 320 vs 285 is +12.3%.
        composeTestRule.onNodeWithContentDescription(string(R.string.spending_down_percent, "3%")).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(string(R.string.spending_up_percent, "12%")).assertIsDisplayed()
        // Health and Miscellaneous spent nothing in April.
        composeTestRule.onAllNodesWithContentDescription(string(R.string.spending_new_description), useUnmergedTree = true)
            .assertCountEquals(2)
    }

    @Test
    fun tappingARow_opensThatCategory() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.dining)).performClick()

        assertThat(events).containsExactly("category:${CATEGORIES.DINING.value}")
    }

    @Test
    fun monthControls_followTheState() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.next_month)).assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(string(R.string.previous_month)).assertIsEnabled().performClick()

        assertThat(events).containsExactly("previous")
    }

    @Test
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(events).containsExactly("back")
    }
}
