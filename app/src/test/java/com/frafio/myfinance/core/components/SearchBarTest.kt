package com.frafio.myfinance.core.components

import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
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
    fun typing_updatesTheQuery() {
        val query = TextFieldState()
        composeTestRule.setThemedContent {
            SearchBar(state = query, onFilterClick = {})
        }

        composeTestRule.onNodeWithTag("search_field").performTextInput("latte")

        assertThat(query.text.toString()).isEqualTo("latte")
        composeTestRule.onNodeWithTag("search_field").assert(hasText("latte"))
    }

    @Test
    fun placeholder_isShownWhenTheQueryIsEmpty() {
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState(), onFilterClick = {})
        }

        composeTestRule.onNodeWithText(string(R.string.search)).assertIsDisplayed()
    }

    @Test
    fun clearButton_isAbsentWithoutAQuery() {
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState(), onFilterClick = {})
        }

        composeTestRule.onNode(clearButton).assertDoesNotExist()
    }

    @Test
    fun clearButton_isShownWithAQuery() {
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState("x"), onFilterClick = {})
        }

        composeTestRule.onNode(clearButton).assertIsDisplayed()
    }

    @Test
    fun clearButton_click_clearsTheQuery() {
        val query = TextFieldState("latte")
        composeTestRule.setThemedContent {
            SearchBar(state = query, onFilterClick = {})
        }

        composeTestRule.onNode(clearButton).performClick()

        assertThat(query.text.toString()).isEmpty()
    }

    @Test
    fun filterButton_click_invokesOnFilterClick() {
        var clicks = 0
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState(), onFilterClick = { clicks++ })
        }

        composeTestRule.onNodeWithTag("search_filter_button").performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun filterButton_isDescribedForAccessibility() {
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState(), onFilterClick = {})
        }

        composeTestRule.onNodeWithContentDescription(string(R.string.filter)).assertIsDisplayed()
    }

    @Test
    fun collapsed_showsNoBackButton() {
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState(), onFilterClick = {})
        }

        composeTestRule.onNodeWithTag("search_back_button").assertDoesNotExist()
    }

    @Test
    fun focusingTheField_reportsExpanded() {
        val reported = mutableListOf<Boolean>()
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState(), onFilterClick = {}, onExpandedChange = { reported += it })
        }

        composeTestRule.onNodeWithTag("search_field").performClick()

        assertThat(reported).containsExactly(true)
    }

    @Test
    fun expanded_backButton_isDescribedForAccessibility() {
        composeTestRule.setThemedContent {
            SearchBar(state = rememberTextFieldState(), onFilterClick = {}, expanded = true)
        }

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).assertIsDisplayed()
    }

    // Focus itself is not asserted after the tap: Robolectric hands it back to the only focusable
    // once it is cleared, which a device does not do.
    @Test
    fun backButton_collapses_withoutExpandingAgain() {
        val reported = mutableListOf<Boolean>()
        composeTestRule.setThemedContent {
            var expanded by remember { mutableStateOf(false) }
            SearchBar(state = rememberTextFieldState(), onFilterClick = {}, expanded = expanded, onExpandedChange = { reported += it; expanded = it })
        }

        composeTestRule.onNodeWithTag("search_field").performClick()
        composeTestRule.onNodeWithTag("search_field").assertIsFocused()
        composeTestRule.onNodeWithTag("search_back_button").performClick()

        assertThat(reported).containsExactly(true, false).inOrder()
        composeTestRule.onNodeWithTag("search_back_button").assertDoesNotExist()
    }

    @Test
    fun systemBack_whileExpanded_reportsCollapsed() {
        val reported = mutableListOf<Boolean>()
        var dispatcher: OnBackPressedDispatcher? = null
        composeTestRule.setThemedContent {
            dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            SearchBar(state = rememberTextFieldState(), onFilterClick = {}, expanded = true, onExpandedChange = { reported += it })
        }

        composeTestRule.runOnIdle { dispatcher!!.onBackPressed() }

        assertThat(reported).containsExactly(false)
    }
}
