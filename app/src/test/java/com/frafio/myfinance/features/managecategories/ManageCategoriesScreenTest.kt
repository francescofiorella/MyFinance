package com.frafio.myfinance.features.managecategories

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.ExpenseCategories
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
class ManageCategoriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var backClicks = 0

    private fun setScreen() {
        composeTestRule.setThemedContent {
            ManageCategoriesContent(categories = ExpenseCategories.all, onBackClick = { backClicks++ })
        }
    }

    private fun hasStateDescription(state: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, state)

    @Test
    fun listsEveryCategory_collapsed() {
        setScreen()

        ExpenseCategories.all.forEach { category ->
            // A lazy list only composes what is on screen, so scroll to each row first.
            composeTestRule.onNodeWithTag("manage_categories_list").performScrollToNode(hasText(string(category.nameRes)))
            composeTestRule.onNodeWithText(string(category.nameRes)).assertIsDisplayed()
            composeTestRule.onNodeWithText(string(category.descriptionRes)).assertDoesNotExist()
        }
    }

    @Test
    fun tap_expandsTheDescription_andTappingAnotherCollapsesTheFirst() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.housing)).performClick()
        composeTestRule.onNodeWithText(string(R.string.housing_description)).assertIsDisplayed()

        composeTestRule.onNodeWithText(string(R.string.groceries)).performClick()
        composeTestRule.onNodeWithText(string(R.string.groceries_description)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.housing_description)).assertDoesNotExist()

        composeTestRule.onNodeWithText(string(R.string.groceries)).performClick()
        composeTestRule.onNodeWithText(string(R.string.groceries_description)).assertDoesNotExist()
    }

    @Test
    fun row_reportsWhetherItIsExpanded() {
        setScreen()
        val housing = composeTestRule.onNodeWithText(string(R.string.housing))

        housing.assert(hasStateDescription(string(R.string.collapsed))).performClick()

        housing.assert(hasStateDescription(string(R.string.expanded)))
    }

    @Test
    fun showsTheCategoriesTitle() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.categories)).assertIsDisplayed()
    }

    @Test
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(backClicks).isEqualTo(1)
    }
}
