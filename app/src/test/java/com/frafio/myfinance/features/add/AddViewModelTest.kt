package com.frafio.myfinance.features.add

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import com.frafio.myfinance.features.add.AddViewModel.Companion.REQUEST_EXPENSE_CODE
import com.frafio.myfinance.features.add.AddViewModel.Companion.REQUEST_INCOME_CODE
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.data.testPreferences
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestIncomeRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class AddViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesRepository = TestExpensesRepository()
    private val incomeRepository = TestIncomeRepository()
    private val loadingRepository = LoadingRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()

    private fun viewModel(navKey: RootKey.AddEditTransaction) = AddViewModel(
        expensesRepository, incomeRepository, loadingRepository, userPreferencesRepository, navKey,
    )

    private val addExpense = RootKey.AddEditTransaction(RootKey.RequestType.Add, REQUEST_EXPENSE_CODE)
    private val addIncome = RootKey.AddEditTransaction(RootKey.RequestType.Add, REQUEST_INCOME_CODE)

    // region initial state

    @Test
    fun addMode_startsBlankOnToday() {
        val viewModel = viewModel(addExpense)
        val today = LocalDate.now()

        assertThat(viewModel.name).isEmpty()
        assertThat(viewModel.priceString).isEmpty()
        assertThat(viewModel.category).isEqualTo(-1)
        assertThat(viewModel.labels).isEmpty()
        assertThat(viewModel.expenseId).isEmpty()
        assertThat(viewModel.year).isEqualTo(today.year)
        assertThat(viewModel.month).isEqualTo(today.monthValue)
        assertThat(viewModel.day).isEqualTo(today.dayOfMonth)
        assertThat(viewModel.isAdding.value).isFalse()
    }

    @Test
    fun editMode_isPrefilledFromTheTransaction() {
        val expense = testExpense(
            name = "Rent", price = 12.5, date = LocalDate.of(2023, 4, 9),
            category = FirestoreEnums.CATEGORIES.HOUSING.value, labels = listOf("home"), id = "abc",
        )
        val viewModel = viewModel(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_EXPENSE_CODE, expense))

        assertThat(viewModel.name).isEqualTo("Rent")
        assertThat(viewModel.priceString).isEqualTo("12.5")
        assertThat(viewModel.category).isEqualTo(FirestoreEnums.CATEGORIES.HOUSING.value)
        assertThat(viewModel.labels).containsExactly("home")
        assertThat(viewModel.expenseId).isEqualTo("abc")
        assertThat(viewModel.year).isEqualTo(2023)
        assertThat(viewModel.month).isEqualTo(4)
        assertThat(viewModel.day).isEqualTo(9)
    }

    @Test
    fun priceString_dropsDecimalsForWholeAmounts() {
        val whole = viewModel(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_EXPENSE_CODE, testExpense(price = 12.0)))
        val fractional = viewModel(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_EXPENSE_CODE, testExpense(price = 12.5)))

        assertThat(whole.priceString).isEqualTo("12")
        assertThat(fractional.priceString).isEqualTo("12.5")
    }

    @Test
    fun dateString_reflectsTheCurrentDateFields() {
        val viewModel = viewModel(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_EXPENSE_CODE, testExpense(date = LocalDate.of(2024, 1, 5))))
        assertThat(viewModel.dateString).isEqualTo("05 Jan 2024")

        viewModel.month = 12
        viewModel.day = 25
        assertThat(viewModel.dateString).isEqualTo("25 Dec 2024")
    }

    @Test
    fun allLabelsAndCurrency_areInitialisedFromPreferences() {
        userPreferencesRepository.setPreferences(testPreferences(labels = listOf("a", "b"), currencyCode = "USD"))
        val viewModel = viewModel(addExpense)

        assertThat(viewModel.allLabels.value).containsExactly("a", "b").inOrder()
        assertThat(viewModel.currencyCode.value).isEqualTo("USD")
    }

    @Test
    fun allLabelsAndCurrency_followPreferences() = runTest {
        val viewModel = viewModel(addExpense)
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.allLabels.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.currencyCode.collect() }

        userPreferencesRepository.setPreferences(testPreferences(labels = listOf("z"), currencyCode = "GBP"))

        assertThat(viewModel.allLabels.value).containsExactly("z")
        assertThat(viewModel.currencyCode.value).isEqualTo("GBP")
    }

    @Test
    fun onLabelCheckedChanged_addsOnceAndRemoves() {
        val viewModel = viewModel(addExpense)

        viewModel.onLabelCheckedChanged("a", true)
        viewModel.onLabelCheckedChanged("a", true)
        viewModel.onLabelCheckedChanged("b", true)
        assertThat(viewModel.labels).containsExactly("a", "b").inOrder()

        viewModel.onLabelCheckedChanged("a", false)
        assertThat(viewModel.labels).containsExactly("b")
    }

    @Test
    fun updateAddingState_togglesIsAdding() {
        val viewModel = viewModel(addExpense)
        viewModel.updateAddingState(true)
        assertThat(viewModel.isAdding.value).isTrue()
        viewModel.updateAddingState(false)
        assertThat(viewModel.isAdding.value).isFalse()
    }

    // endregion

    // region onAddButtonClick

    @Test
    fun add_expense_buildsTheExpenseAndEmitsSuccess() = runTest {
        val viewModel = viewModel(addExpense)
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("  Coffee ", " 2.5 ", 5, 2024, 3, 7, listOf("cafe"))

        val added = expensesRepository.addedExpenses.single()
        assertThat(added.name).isEqualTo("Coffee")
        assertThat(added.price).isEqualTo(2.5)
        assertThat(added.year).isEqualTo(2024)
        assertThat(added.month).isEqualTo(3)
        assertThat(added.day).isEqualTo(7)
        assertThat(added.timestamp).isEqualTo(dateToUTCTimestamp(2024, 3, 7))
        assertThat(added.category).isEqualTo(5)
        assertThat(added.labels).containsExactly("cafe")
        assertThat(added.id).isEqualTo("Coffee2.5${dateToUTCTimestamp(2024, 3, 7)}5[cafe]")
        assertThat(events).containsExactly(AddUiEvent.Success(expensesRepository.addResult, true, 7, 3, 2024))
    }

    @Test
    fun add_expense_failure_emitsError() = runTest {
        expensesRepository.addResult = FinanceResult(FinanceCode.EXPENSE_ADD_FAILURE)
        val viewModel = viewModel(addExpense)
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("Coffee", "2.5", 5, 2024, 3, 7, emptyList())

        assertThat(events).containsExactly(AddUiEvent.Error(expensesRepository.addResult))
    }

    @Test
    fun add_income_forcesIncomeCategoryAndDropsLabels() = runTest {
        val viewModel = viewModel(addIncome)
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("Salary", "1000", 5, 2024, 3, 31, listOf("ignored"))

        val added = incomeRepository.addedIncomes.single()
        assertThat(added.name).isEqualTo("Salary")
        assertThat(added.price).isEqualTo(1000.0)
        assertThat(added.category).isEqualTo(FirestoreEnums.CATEGORIES.INCOME.value)
        assertThat(added.labels).isEmpty()
        assertThat(added.timestamp).isEqualTo(dateToUTCTimestamp(2024, 3, 31))
        assertThat(expensesRepository.addedExpenses).isEmpty()
        assertThat(events).containsExactly(AddUiEvent.Success(incomeRepository.addResult, false, 31, 3, 2024))
    }

    @Test
    fun add_income_failure_emitsError() = runTest {
        incomeRepository.addResult = FinanceResult(FinanceCode.INCOME_ADD_FAILURE)
        val viewModel = viewModel(addIncome)
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("Salary", "1000", 5, 2024, 3, 31, emptyList())

        assertThat(events).containsExactly(AddUiEvent.Error(incomeRepository.addResult))
    }

    @Test
    fun edit_expense_keepsTheOriginalId() = runTest {
        val original = testExpense(id = "keep-me")
        val viewModel = viewModel(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_EXPENSE_CODE, original))
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("Renamed", "9", 1, 2024, 1, 2, listOf("x"))

        val edited = expensesRepository.editedExpenses.single()
        assertThat(edited.id).isEqualTo("keep-me")
        assertThat(edited.name).isEqualTo("Renamed")
        assertThat(edited.price).isEqualTo(9.0)
        assertThat(edited.labels).containsExactly("x")
        assertThat(expensesRepository.addedExpenses).isEmpty()
        assertThat(events).containsExactly(AddUiEvent.Success(expensesRepository.editResult, true, 2, 1, 2024))
    }

    @Test
    fun edit_expense_failure_emitsError() = runTest {
        expensesRepository.editResult = FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
        val viewModel = viewModel(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_EXPENSE_CODE, testExpense()))
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("X", "1", 1, 2024, 1, 2, emptyList())

        assertThat(events).containsExactly(AddUiEvent.Error(expensesRepository.editResult))
    }

    @Test
    fun edit_income_keepsTheOriginalIdAndDropsLabels() = runTest {
        val original = testIncome(id = "keep-me")
        val viewModel = viewModel(RootKey.AddEditTransaction(RootKey.RequestType.Edit, REQUEST_INCOME_CODE, original))
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("Bonus", "50", 7, 2024, 6, 30, listOf("ignored"))

        val edited = incomeRepository.editedIncomes.single()
        assertThat(edited.id).isEqualTo("keep-me")
        assertThat(edited.category).isEqualTo(FirestoreEnums.CATEGORIES.INCOME.value)
        assertThat(edited.labels).isEmpty()
        assertThat(events).containsExactly(AddUiEvent.Success(incomeRepository.editResult, false, 30, 6, 2024))
    }

    @Test
    fun onAddButtonClick_bracketsWithLoadingAndIsAdding() = runTest {
        val viewModel = viewModel(addExpense)
        val loading = collectLoading()
        val adding = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isAdding.toList(adding) }
        collectEvents(viewModel)

        viewModel.onAddButtonClick("X", "1", 1, 2024, 1, 2, emptyList())

        assertThat(loading).containsExactly(false, true, false).inOrder()
        assertThat(adding).containsExactly(false, true, false).inOrder()
    }

    @Test
    fun onAddButtonClick_unparseablePrice_emitsWrongAmountInsteadOfCrashing() = runTest {
        val viewModel = viewModel(addExpense)
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("X", "abc", 1, 2024, 1, 2, emptyList())

        assertThat(events).containsExactly(AddUiEvent.Error(FinanceResult(FinanceCode.WRONG_AMOUNT)))
        assertThat(expensesRepository.addedExpenses).isEmpty()
        assertThat(loadingRepository.isLoading.value).isFalse()
        assertThat(viewModel.isAdding.value).isFalse()
    }

    @Test
    fun onAddButtonClick_emptyPrice_emitsEmptyAmountInsteadOfCrashing() = runTest {
        val viewModel = viewModel(addExpense)
        val events = collectEvents(viewModel)

        viewModel.onAddButtonClick("X", "   ", 1, 2024, 1, 2, emptyList())

        assertThat(events).containsExactly(AddUiEvent.Error(FinanceResult(FinanceCode.EMPTY_AMOUNT)))
        assertThat(expensesRepository.addedExpenses).isEmpty()
    }

    // endregion

    private fun TestScope.collectEvents(viewModel: AddViewModel): List<AddUiEvent> {
        val events = mutableListOf<AddUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiEvents.toList(events) }
        return events
    }

    private fun TestScope.collectLoading(): List<Boolean> {
        val states = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { loadingRepository.isLoading.toList(states) }
        return states
    }
}
