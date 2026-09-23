package com.frafio.myfinance.core.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.core.data.model.Transaction
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.util.setThemedContent
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Rendered inline (`LocalInspectionMode`), which bypasses `ModalBottomSheet` and shows the content directly. */
@RunWith(RobolectricTestRunner::class)
class EditTransactionSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
        events.clear()
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    private fun setSheet(show: Boolean = true, transaction: Transaction) {
        composeTestRule.setThemedContent(inline = true) {
            EditTransactionSheet(
                show = show,
                transaction = transaction,
                onDismiss = { events += "dismiss" },
                onLabels = { events += "labels" },
                onEdit = { events += "edit" },
                onDuplicate = { events += "duplicate" },
                onDelete = { events += "delete" },
            )
        }
    }

    @Test
    fun expense_showsLabelsEditDuplicateDelete_inThatOrder() {
        setSheet(transaction = testExpense())

        val tops = listOf("transaction_labels", "transaction_edit", "transaction_duplicate", "transaction_delete")
            .map { composeTestRule.onNodeWithTag(it).assertIsDisplayed().getBoundsInRoot().top }

        assertThat(tops).isInOrder()
        assertThat(tops.toSet()).hasSize(4)
    }

    @Test
    fun income_hasNoLabelsItem() {
        setSheet(transaction = testIncome())

        composeTestRule.onNodeWithTag("transaction_labels").assertDoesNotExist()
        composeTestRule.onNodeWithTag("transaction_edit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("transaction_duplicate").assertIsDisplayed()
        composeTestRule.onNodeWithTag("transaction_delete").assertIsDisplayed()
    }

    @Test
    fun header_showsNameDateAndPrice() {
        setSheet(transaction = testExpense(name = "Pizza", price = 13.0))

        composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
        composeTestRule.onNodeWithText("15/01/2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 13.00").assertIsDisplayed()
    }

    @Test
    fun income_header_showsTheFirstLetterInsteadOfAnIcon() {
        setSheet(transaction = testIncome(name = "salary"))

        composeTestRule.onNodeWithText("S").assertIsDisplayed()
    }

    @Test
    fun eachItem_invokesItsCallbackThenDismisses() {
        setSheet(transaction = testExpense())

        composeTestRule.onNodeWithTag("transaction_labels").performClick()
        composeTestRule.onNodeWithTag("transaction_edit").performClick()
        composeTestRule.onNodeWithTag("transaction_duplicate").performClick()
        composeTestRule.onNodeWithTag("transaction_delete").performClick()

        assertThat(events).containsExactly(
            "labels", "dismiss", "edit", "dismiss", "duplicate", "dismiss", "delete", "dismiss",
        ).inOrder()
    }

    @Test
    fun showFalse_rendersNothing() {
        setSheet(show = false, transaction = testExpense(name = "Pizza"))

        composeTestRule.onNodeWithText("Pizza").assertDoesNotExist()
        composeTestRule.onNodeWithTag("transaction_edit").assertDoesNotExist()
    }
}
