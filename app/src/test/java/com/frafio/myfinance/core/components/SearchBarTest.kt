package com.frafio.myfinance.core.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // The clear button is the only clickable inside the text field; the filter button sits beside it.
    private val clearButton = hasClickAction() and hasAnyAncestor(hasTestTag("search_field"))

    @Test
    fun typing_reportsTheQuery() {
        val reported = mutableListOf<String>()
        composeTestRule.setThemedContent {
            var query by remember { mutableStateOf("") }
            SearchBar(query = query, onQueryChange = { query = it; reported += it }, onFilterClick = {})
        }

        composeTestRule.onNodeWithTag("search_field").performTextInput("latte")

        assertThat(reported.last()).isEqualTo("latte")
        composeTestRule.onNodeWithTag("search_field").assert(hasText("latte"))
    }

    @Test
    fun placeholder_isShownWhenTheQueryIsEmpty() {
        composeTestRule.setThemedContent {
            SearchBar(query = "", onQueryChange = {}, onFilterClick = {})
        }

        composeTestRule.onNodeWithText(string(R.string.search)).assertIsDisplayed()
    }

    @Test
    fun clearButton_isAbsentWithoutAQuery() {
        composeTestRule.setThemedContent {
            SearchBar(query = "", onQueryChange = {}, onFilterClick = {})
        }

        composeTestRule.onNode(clearButton).assertDoesNotExist()
    }

    @Test
    fun clearButton_isShownWithAQuery() {
        composeTestRule.setThemedContent {
            SearchBar(query = "x", onQueryChange = {}, onFilterClick = {})
        }

        composeTestRule.onNode(clearButton).assertIsDisplayed()
    }

    @Test
    fun clearButton_click_reportsAnEmptyQuery() {
        val reported = mutableListOf<String>()
        composeTestRule.setThemedContent {
            SearchBar(query = "latte", onQueryChange = { reported += it }, onFilterClick = {})
        }

        composeTestRule.onNode(clearButton).performClick()

        assertThat(reported).containsExactly("")
    }

    @Test
    fun filterButton_click_invokesOnFilterClick() {
        var clicks = 0
        composeTestRule.setThemedContent {
            SearchBar(query = "", onQueryChange = {}, onFilterClick = { clicks++ })
        }

        composeTestRule.onNodeWithTag("search_filter_button").performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun filterButton_isDescribedForAccessibility() {
        composeTestRule.setThemedContent {
            SearchBar(query = "", onQueryChange = {}, onFilterClick = {})
        }

        composeTestRule.onNodeWithContentDescription(string(R.string.filter)).assertIsDisplayed()
    }
}
