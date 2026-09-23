package com.frafio.myfinance.core.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class TransactionItemsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val date = LocalDate.of(2024, 1, 15)

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    // region TotalItem

    @Test
    fun totalItem_expense_showsExtendedDateAndPrice() {
        composeTestRule.setThemedContent {
            TotalItem(transaction = testExpense(price = 1.5, date = date, category = FirestoreEnums.CATEGORIES.TOTAL.value))
        }

        composeTestRule.onNodeWithText("15 Jan 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 1.50").assertIsDisplayed()
    }

    @Test
    fun totalItem_income_showsTheYear() {
        composeTestRule.setThemedContent {
            TotalItem(transaction = testIncome(price = 2500.0, date = date))
        }

        composeTestRule.onNodeWithText("2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("15 Jan 2024").assertDoesNotExist()
        composeTestRule.onNodeWithText("€ 2500.00").assertIsDisplayed()
    }

    @Test
    fun totalItem_negativePrice_hidesTheAmount() {
        composeTestRule.setThemedContent {
            TotalItem(transaction = testExpense(price = -3.0, date = date))
        }

        composeTestRule.onNodeWithText("15 Jan 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ -3.00").assertDoesNotExist()
    }

    // endregion

    // region TransactionListItem

    @Test
    fun listItem_expense_showsNameCategoryAndPrice() {
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testExpense(name = "Pizza", price = 13.0, category = FirestoreEnums.CATEGORIES.DINING.value),
                indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {},
            )
        }

        composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.dining)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 13.00").assertIsDisplayed()
    }

    @Test
    fun listItem_income_showsNameShortDateAndPrice() {
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testIncome(name = "Salary", price = 2500.0, date = date),
                indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {},
            )
        }

        composeTestRule.onNodeWithText("Salary").assertIsDisplayed()
        composeTestRule.onNodeWithText("15/01/2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 2500.00").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.dining)).assertDoesNotExist()
    }

    @Test
    fun listItem_income_showsFirstLetterAvatar() {
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testIncome(name = "salary"),
                indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {},
            )
        }

        composeTestRule.onNodeWithText("S").assertIsDisplayed()
    }

    @Test
    fun listItem_income_withEmptyName_rendersWithoutCrashing() {
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testIncome(name = ""),
                indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {},
            )
        }

        composeTestRule.onNodeWithText("31/01/2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 1000.00").assertIsDisplayed()
    }

    @Test
    fun listItem_expense_showsLabelsAsChips() {
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testExpense(name = "Pizza", labels = listOf("Dinner", "Cheat Meal")),
                indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {},
            )
        }

        composeTestRule.onNodeWithText("Dinner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cheat Meal").assertIsDisplayed()
    }

    @Test
    fun listItem_expense_withoutLabels_showsNoChips() {
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testExpense(name = "Pizza"),
                indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {},
            )
        }

        composeTestRule.onNodeWithText("Dinner").assertDoesNotExist()
    }

    @Test
    fun listItem_click_invokesOnClick() {
        var clicks = 0
        var longClicks = 0
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testExpense(name = "Pizza"),
                indexInGroup = 0, countInGroup = 1, onClick = { clicks++ }, onLongClick = { longClicks++ },
            )
        }

        composeTestRule.onNodeWithText("Pizza").performClick()

        assertThat(clicks).isEqualTo(1)
        assertThat(longClicks).isEqualTo(0)
    }

    @Test
    fun listItem_longClick_invokesOnLongClick() {
        var clicks = 0
        var longClicks = 0
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testExpense(name = "Pizza"),
                indexInGroup = 0, countInGroup = 1, onClick = { clicks++ }, onLongClick = { longClicks++ },
            )
        }

        composeTestRule.onNodeWithText("Pizza").performTouchInput { longClick() }

        assertThat(longClicks).isEqualTo(1)
        assertThat(clicks).isEqualTo(0)
    }

    @Test
    fun listItem_expense_iconClick_invokesOnIconClick() {
        var iconClicks = 0
        var rowClicks = 0
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testExpense(name = "Pizza"),
                indexInGroup = 0, countInGroup = 1,
                onClick = { rowClicks++ }, onLongClick = {}, onIconClick = { iconClicks++ },
            )
        }

        // The category icon is the only clickable nested inside the clickable row.
        composeTestRule
            .onNode(hasClickAction() and hasAnyAncestor(hasClickAction()))
            .performClick()

        assertThat(iconClicks).isEqualTo(1)
        assertThat(rowClicks).isEqualTo(0)
    }

    // endregion

    @Test
    fun emptyListItem_showsTheMessage() {
        composeTestRule.setThemedContent {
            EmptyListItem(messageRes = R.string.no_expenses)
        }

        composeTestRule.onNodeWithText(string(R.string.no_expenses)).assertIsDisplayed()
    }
}
