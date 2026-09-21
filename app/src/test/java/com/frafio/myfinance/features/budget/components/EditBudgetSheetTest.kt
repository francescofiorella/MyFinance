package com.frafio.myfinance.features.budget.components

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
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
class EditBudgetSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    private fun setSheet(budget: Double) {
        composeTestRule.setThemedContent(inline = true) {
            EditBudgetSheet(show = true, budget = budget, onDismiss = { events += "dismiss" }, onEditBudget = { events += "budget:$it" })
        }
    }

    private fun field() = composeTestRule.onNodeWithText(string(R.string.enter_your_budget))
    private fun confirm() = composeTestRule.onNodeWithContentDescription(string(R.string.confirm))

    private fun type(text: String) {
        field().performTextClearance()
        field().performTextInput(text)
    }

    @Test
    fun existingBudget_isPrefilled_andConfirmStartsDisabled() {
        setSheet(1000.0)

        field().assert(hasText("1000.00"))
        confirm().assertIsNotEnabled()
    }

    @Test
    fun noBudget_startsEmpty_andConfirmDisabled() {
        setSheet(0.0)

        field().assert(hasText(""))
        confirm().assertIsNotEnabled()
    }

    @Test
    fun newValue_isSent_thenDismisses() {
        setSheet(1000.0)
        type("1500")

        confirm().assertIsEnabled().performClick()

        assertThat(events).containsExactly("budget:1500.0", "dismiss").inOrder()
    }

    @Test
    fun sameValue_keepsConfirmDisabled() {
        setSheet(1000.0)
        type("1000")

        confirm().assertIsNotEnabled()
    }

    @Test
    fun decimals_areTruncatedToTwo() {
        setSheet(0.0)
        type("12.345")

        field().assert(hasText("12.34"))
    }

    @Test
    fun nonNumericText_keepsConfirmDisabled() {
        setSheet(1000.0)
        type("abc")

        confirm().assertIsNotEnabled().performClick()

        assertThat(events).isEmpty()
    }
}
