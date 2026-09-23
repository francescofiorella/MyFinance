package com.frafio.myfinance.features.expenses

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.core.utils.getCategoryName
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestExpensesRepository
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
class ExpensesScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val expensesRepository = TestExpensesRepository()
    private val expensesLocalRepository = TestExpensesLocalRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: ExpensesViewModel

    private val events = mutableListOf<ExpensesUiEvent>()
    private val edited = mutableListOf<Pair<Expense, Int>>()
    private val duplicated = mutableListOf<Expense>()

    // Totals are computed against the real today; the fixtures stay relative to it.
    private val today: LocalDate = LocalDate.now()
    private val pizza = testExpense(name = "Pizza", price = 8.5, date = today, category = CATEGORIES.DINING.value, id = "pizza")
    private val bus = testExpense(name = "Bus", price = 1.5, date = today.minusDays(1), category = CATEGORIES.TRANSPORTATION.value, id = "bus")

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
        userPreferencesRepository.setLabels(listOf("Dinner"))
        viewModel = ExpensesViewModel(expensesRepository, expensesLocalRepository, userPreferencesRepository, loadingRepository, UnconfinedTestDispatcher())
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    private fun setScreen() {
        composeTestRule.setThemedContent(inline = true) {
            LaunchedEffect(Unit) { viewModel.uiEvents.collect { events += it } }
            ExpensesScreen(
                viewModel = viewModel,
                onEditExpense = { expense, index -> edited += expense to index },
                onDuplicateExpense = { duplicated += it },
                getDateLabel = { _, _ -> "range" },
            )
        }
    }

    private fun populated() {
        loadingRepository.stopFirstSync()
        expensesLocalRepository.sendExpenses(pizza, bus)
        setScreen()
    }

    private fun longPress(name: String) = composeTestRule.onNodeWithText(name).performTouchInput { longClick() }

    @Test
    fun firstSync_showsLoading() {
        setScreen()

        composeTestRule.onNodeWithTag("expenses_list").assertDoesNotExist()
        composeTestRule.onNodeWithText(string(R.string.warning_home)).assertDoesNotExist()
    }

    @Test
    fun noExpenses_showsTheEmptyView() {
        loadingRepository.stopFirstSync()
        expensesLocalRepository.sendExpenses(emptyList())
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.warning_home)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_field").assertDoesNotExist()
    }

    @Test
    fun withExpenses_showsSearchAndTheList() {
        populated()

        composeTestRule.onNodeWithTag("search_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("expenses_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bus").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("expense_item").assertCountEquals(2)
    }

    @Test
    fun search_filtersTheList() {
        populated()

        composeTestRule.onNodeWithTag("search_field").performTextInput("Piz")

        assertThat(expensesLocalRepository.filterCalls.last().name).isEqualTo("Piz")
        composeTestRule.onNodeWithText("Pizza").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bus").assertDoesNotExist()
    }

    @Test
    fun filterByCategory_showsTheChip_andRemovingItClears() {
        populated()

        composeTestRule.onNodeWithTag("search_filter_button").performClick()
        composeTestRule.onNodeWithTag("filter_category").performClickAction()
        // Groceries: no row carries that category name, so the grid item is the only match.
        composeTestRule.onNodeWithText(string(R.string.groceries)).performClickAction()

        assertThat(viewModel.selectedCategories.value).containsExactly(CATEGORIES.GROCERIES.value)
        assertThat(expensesLocalRepository.filterCalls.last().categories).containsExactly(CATEGORIES.GROCERIES.value)
        composeTestRule.onNodeWithText("Bus").assertDoesNotExist()
        composeTestRule.onNodeWithText("Pizza").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(string(R.string.remove_item, string(R.string.groceries))).performClick()

        assertThat(viewModel.selectedCategories.value).isEmpty()
        composeTestRule.onNodeWithText("Bus").assertIsDisplayed()
    }

    @Test
    fun longPress_edit_forwardsTheExpenseAndItsListIndex() {
        populated()

        longPress("Pizza")
        composeTestRule.onNodeWithTag("transaction_edit").performClickAction()

        // The index is the position in the list with its total rows, which the ViewModel exposes.
        assertThat(edited).containsExactly(pizza to viewModel.expenses.value.indexOf(pizza))
    }

    @Test
    fun longPress_duplicate_forwardsTheExpense() {
        populated()

        longPress("Bus")
        composeTestRule.onNodeWithTag("transaction_duplicate").performClickAction()

        assertThat(duplicated).containsExactly(bus)
    }

    @Test
    fun longPress_delete_asksForConfirmation_thenDeletes() {
        populated()

        longPress("Pizza")
        composeTestRule.onNodeWithTag("transaction_delete").performClickAction()
        assertThat(expensesRepository.deletedExpenses).isEmpty()
        composeTestRule.onNodeWithText(string(R.string.delete_permanently)).performClickAction()

        assertThat(expensesRepository.deletedExpenses).containsExactly(pizza)
        assertThat(events).containsExactly(ExpensesUiEvent.ExpenseDeleted(pizza))
    }

    @Test
    fun categoryIcon_opensTheCategorySheet_andUpdatesTheExpense() {
        populated()

        // The category icon is the clickable nested inside the row.
        composeTestRule.onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag("expense_item")))[0].performClick()
        composeTestRule.onNodeWithText(string(getCategoryName(CATEGORIES.GROCERIES.value))).performClickAction()

        assertThat(expensesRepository.editedExpenses.single().category).isEqualTo(CATEGORIES.GROCERIES.value)
    }
}
