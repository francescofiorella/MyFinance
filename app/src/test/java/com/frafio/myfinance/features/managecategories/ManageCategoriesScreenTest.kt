package com.frafio.myfinance.features.managecategories

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

    @Test
    fun listsEveryCategory_withItsDescription() {
        setScreen()

        ExpenseCategories.all.forEach { category ->
            // A lazy list only composes what is on screen, so scroll to each row first.
            composeTestRule.onNodeWithTag("manage_categories_list").performScrollToNode(hasText(string(category.descriptionRes)))
            composeTestRule.onNodeWithText(string(category.nameRes)).assertIsDisplayed()
            composeTestRule.onNodeWithText(string(category.descriptionRes)).assertIsDisplayed()
        }
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
