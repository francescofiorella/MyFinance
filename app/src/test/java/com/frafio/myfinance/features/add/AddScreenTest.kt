package com.frafio.myfinance.features.add

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.features.add.AddViewModel.Companion.REQUEST_EXPENSE_CODE
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestIncomeRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.frafio.myfinance.testing.util.performClickAction
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/** The stateful Add/Edit screen: its own validation, then the ViewModel; navigation stays in the entry. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class AddScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val expensesRepository = TestExpensesRepository()
    private val incomeRepository = TestIncomeRepository()
    private val events = mutableListOf<AddUiEvent>()
    private var backClicks = 0

    private val addExpense = RootKey.AddEditTransaction(RootKey.RequestType.Add, REQUEST_EXPENSE_CODE)

    private fun setScreen(navKey: RootKey.AddEditTransaction = addExpense) {
        val viewModel = AddViewModel(expensesRepository, incomeRepository, LoadingRepository(), TestUserPreferencesRepository(), navKey)
        composeTestRule.setThemedContent(inline = true) {
            LaunchedEffect(Unit) { viewModel.uiEvents.collect { events += it } }
            AddScreen(appState = rememberMyFinanceAppState(), viewModel = viewModel, onBackClick = { backClicks++ })
        }
    }

    private fun typeNameAndAmount(name: String, amount: String) {
        composeTestRule.onNodeWithTag("add_name_field").performTextInput(name)
        composeTestRule.onNodeWithTag("add_amount_field").performTextInput(amount)
    }

    private fun pickCategory(nameRes: Int) {
        composeTestRule.onNodeWithText(string(R.string.category)).performClick()
        composeTestRule.onNodeWithText(string(nameRes)).performClickAction()
    }

    private fun save() = composeTestRule.onNodeWithTag("add_save_button").performClick()

    @Test
    fun save_withEmptyForm_showsAllThreeErrors() {
        setScreen()

        save()

        composeTestRule.onNodeWithText(FinanceCode.EMPTY_NAME.message).assertIsDisplayed()
        composeTestRule.onNodeWithText(FinanceCode.EMPTY_AMOUNT.message).assertIsDisplayed()
        composeTestRule.onNodeWithText(FinanceCode.EMPTY_CATEGORY.message).assertIsDisplayed()
        assertThat(expensesRepository.addedExpenses).isEmpty()
    }

    @Test
    fun save_withZeroAmount_showsWrongAmount() {
        setScreen()
        typeNameAndAmount("Pizza", "0")

        save()

        composeTestRule.onNodeWithText(FinanceCode.WRONG_AMOUNT.message).assertIsDisplayed()
    }

    @Test
    fun save_withTotalAsName_isRejected() {
        setScreen()
        typeNameAndAmount(FirestoreEnums.NAMES.TOTAL.value, "5")

        save()

        composeTestRule.onNodeWithText(FinanceCode.WRONG_NAME_TOTAL.message).assertIsDisplayed()
    }

    @Test
    fun typing_clearsTheFieldError() {
        setScreen()
        save()
        composeTestRule.onNodeWithText(FinanceCode.EMPTY_NAME.message).assertIsDisplayed()

        composeTestRule.onNodeWithTag("add_name_field").performTextInput("P")

        composeTestRule.onNodeWithText(FinanceCode.EMPTY_NAME.message).assertDoesNotExist()
        composeTestRule.onNodeWithText(FinanceCode.EMPTY_AMOUNT.message).assertIsDisplayed()
    }

    @Test
    fun expense_valid_isAdded_andTheEntryNavigates() {
        setScreen()
        typeNameAndAmount("Pizza", "8.5")
        pickCategory(R.string.dining)
        composeTestRule.onNodeWithText(string(R.string.dining)).assertIsDisplayed()

        save()

        val added = expensesRepository.addedExpenses.single()
        assertThat(added.name).isEqualTo("Pizza")
        assertThat(added.price).isEqualTo(8.5)
        assertThat(added.category).isEqualTo(FirestoreEnums.CATEGORIES.DINING.value)
        composeTestRule.waitForIdle()
        assertThat(events.single()).isInstanceOf(AddUiEvent.Success::class.java)
        assertThat(backClicks).isEqualTo(0)
    }

    @Test
    fun typeToggle_toIncome_hidesTheCategory_andAddsAnIncome() {
        setScreen()

        composeTestRule.onNodeWithTag("add_type_button").performClick()
        composeTestRule.onAllNodesWithText(string(R.string.income))[0].performClick()
        composeTestRule.onNodeWithText(string(R.string.category)).assertDoesNotExist()
        typeNameAndAmount("Salary", "2500")
        save()

        val added = incomeRepository.addedIncomes.single()
        assertThat(added.name).isEqualTo("Salary")
        assertThat(added.price).isEqualTo(2500.0)
        assertThat(expensesRepository.addedExpenses).isEmpty()
    }

    @Test
    fun editMode_isPrefilled_andSavesAnEdit() {
        val expense = testExpense(name = "Pizza", price = 8.5, date = LocalDate.of(2024, 5, 29), category = FirestoreEnums.CATEGORIES.DINING.value, id = "abc")
        setScreen(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_EXPENSE_CODE, expense, position = 0))

        composeTestRule.onNodeWithTag("add_name_field").assert(hasText("Pizza"))
        composeTestRule.onNodeWithTag("add_amount_field").assert(hasText("8.5"))
        composeTestRule.onNodeWithText("29 May 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.dining)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_type_button").assertIsNotEnabled()

        composeTestRule.onNodeWithTag("add_amount_field").performTextClearance()
        composeTestRule.onNodeWithTag("add_amount_field").performTextInput("9")
        save()

        val edited = expensesRepository.editedExpenses.single()
        assertThat(edited.id).isEqualTo("abc")
        assertThat(edited.price).isEqualTo(9.0)
        assertThat(expensesRepository.addedExpenses).isEmpty()
    }

    @Test
    fun close_invokesOnBack() {
        setScreen()

        composeTestRule.onNodeWithTag("add_close_button").performClick()

        assertThat(backClicks).isEqualTo(1)
    }
}
