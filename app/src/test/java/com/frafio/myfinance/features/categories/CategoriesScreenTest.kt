package com.frafio.myfinance.features.categories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
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
class CategoriesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var backClicks = 0

    private fun setScreen() {
        composeTestRule.setThemedContent { CategoriesScreen(onBackClick = { backClicks++ }) }
    }

    @Test
    fun listsEveryCategory_collapsed() {
        setScreen()

        listOf(R.string.housing, R.string.groceries, R.string.personal_care, R.string.entertainment, R.string.education,
            R.string.dining, R.string.health, R.string.transportation, R.string.miscellaneous)
            .forEach { composeTestRule.onNodeWithText(string(it)).assertIsDisplayed() }
        composeTestRule.onNodeWithText(string(R.string.housing_description)).assertDoesNotExist()
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
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(backClicks).isEqualTo(1)
    }
}
