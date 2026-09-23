package com.frafio.myfinance.features.budget

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestIncomeRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.frafio.myfinance.testing.util.performClickAction
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class BudgetScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val expensesRepository = TestExpensesRepository()
    private val incomeRepository = TestIncomeRepository()
    private val incomesLocalRepository = TestIncomesLocalRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: BudgetViewModel

    private val events = mutableListOf<BudgetUiEvent>()

    // Totals are computed against the real year, so the fixture stays in it.
    private val salary = testIncome(name = "Salary", price = 2500.0, date = LocalDate.now().withDayOfMonth(1), id = "salary")

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
        userPreferencesRepository.setMonthlyBudget(100.0)
        viewModel = BudgetViewModel(expensesRepository, incomeRepository, incomesLocalRepository, userPreferencesRepository, loadingRepository, UnconfinedTestDispatcher())
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    private fun setScreen() {
        composeTestRule.setThemedContent(inline = true) {
            LaunchedEffect(Unit) { viewModel.uiEvents.collect { events += it } }
            BudgetScreen(viewModel = viewModel, onEditIncome = { _, _ -> }, onDuplicateIncome = {})
        }
    }

    private fun populated() {
        loadingRepository.stopFirstSync()
        incomesLocalRepository.sendIncomes(salary)
        setScreen()
    }

    @Test
    fun firstSync_showsLoading() {
        setScreen()

        composeTestRule.onNodeWithTag("budget_list").assertDoesNotExist()
    }

    @Test
    fun noIncomes_showsTheEmptyView_insideTheList() {
        loadingRepository.stopFirstSync()
        incomesLocalRepository.sendIncomes(emptyList())
        setScreen()

        composeTestRule.onNodeWithTag("budget_list").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.warning_budget)).assertIsDisplayed()
    }

    @Test
    fun withIncomes_showsBudgetAndRows() {
        populated()

        composeTestRule.onNodeWithText("€ 100.00").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.annual_budget), substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Salary").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.warning_budget)).assertDoesNotExist()
    }

    @Test
    fun editBudget_opensTheSheet_andSavesTheNewValue() {
        populated()

        // The edit button is the first plain clickable in the overview; delete is the second.
        composeTestRule.onAllNodes(hasClickAction() and !isToggleable())[0].performClick()
        val field = composeTestRule.onNodeWithText(string(R.string.enter_your_budget))
        field.performTextClearance()
        field.performTextInput("250")
        composeTestRule.onNodeWithContentDescription(string(R.string.confirm)).performClickAction()

        assertThat(expensesRepository.budgetCalls).containsExactly(250.0)
        composeTestRule.waitForIdle()
        assertThat(events).containsExactly(BudgetUiEvent.BudgetUpdated(100.0))
    }

    @Test
    fun longPressIncome_hasNoLabelsItem_andDeleteAsksForConfirmation() {
        populated()

        composeTestRule.onNodeWithText("Salary").performTouchInput { longClick() }
        composeTestRule.onNodeWithTag("transaction_labels").assertDoesNotExist()
        composeTestRule.onNodeWithTag("transaction_delete").performClickAction()
        assertThat(incomeRepository.deletedIncomes).isEmpty()
        composeTestRule.onNodeWithText(string(R.string.delete_permanently)).performClickAction()

        assertThat(incomeRepository.deletedIncomes).containsExactly(salary)
        composeTestRule.waitForIdle()
        assertThat(events).containsExactly(BudgetUiEvent.IncomeDeleted(salary))
    }
}
