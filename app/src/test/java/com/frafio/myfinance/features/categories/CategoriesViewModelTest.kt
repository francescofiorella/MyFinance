package com.frafio.myfinance.features.categories

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.SpendingChange
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** The ViewModel starts on the real current month, so every date here is relative to it. */
class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesLocalRepository = TestExpensesLocalRepository()
    private lateinit var viewModel: CategoriesViewModel

    private val thisMonth: LocalDate = LocalDate.now().withDayOfMonth(1)
    private val housing = CATEGORIES.HOUSING.value
    private val dining = CATEGORIES.DINING.value

    @Before
    fun setup() {
        viewModel = CategoriesViewModel(expensesLocalRepository, UnconfinedTestDispatcher())
    }

    @Test
    fun initialState_isTheCurrentMonth_withNothingLoaded() {
        val state = viewModel.uiState.value

        assertThat(state.month).isEqualTo(thisMonth)
        assertThat(state.spending).isEmpty()
        assertThat(state.isPreviousEnabled).isFalse()
        assertThat(state.isNextEnabled).isFalse()
    }

    @Test
    fun uiState_sumsTheSelectedMonth_andComparesWithTheOneBefore() = runTest {
        collectState()

        expensesLocalRepository.sendExpenses(
            testExpense(name = "Rent", price = 600.0, date = thisMonth, category = housing),
            testExpense(name = "Rent", price = 500.0, date = thisMonth.minusMonths(1), category = housing),
            testExpense(name = "Pizza", price = 40.0, date = thisMonth.plusDays(1), category = dining),
            testExpense(name = "Old", price = 999.0, date = thisMonth.minusMonths(2), category = dining),
        )

        val state = viewModel.uiState.value
        assertThat(state.total).isEqualTo(640.0)
        assertThat(state.spending.first().category.id).isEqualTo(housing)
        assertThat(state.spending.first().change).isEqualTo(SpendingChange.Percent(0.2))
        assertThat(state.spending[1].category.id).isEqualTo(dining)
        assertThat(state.spending[1].change).isEqualTo(SpendingChange.New)
    }

    @Test
    fun previous_isEnabledUntilTheEarliestExpenseMonth_andNextUntilTheCurrentMonth() = runTest {
        collectState()
        expensesLocalRepository.sendExpenses(testExpense(date = thisMonth.minusMonths(2)))

        assertThat(viewModel.uiState.value.isPreviousEnabled).isTrue()
        assertThat(viewModel.uiState.value.isNextEnabled).isFalse()

        viewModel.previousMonth()
        viewModel.previousMonth()

        assertThat(viewModel.uiState.value.month).isEqualTo(thisMonth.minusMonths(2))
        assertThat(viewModel.uiState.value.isPreviousEnabled).isFalse()
        assertThat(viewModel.uiState.value.isNextEnabled).isTrue()
    }

    @Test
    fun nextMonth_neverGoesPastTheCurrentMonth_andGoToCurrentMonthReturns() = runTest {
        collectState()
        expensesLocalRepository.sendExpenses(emptyList())

        viewModel.nextMonth()
        assertThat(viewModel.uiState.value.month).isEqualTo(thisMonth)

        viewModel.previousMonth()
        viewModel.previousMonth()
        viewModel.nextMonth()
        assertThat(viewModel.uiState.value.month).isEqualTo(thisMonth.minusMonths(1))

        viewModel.goToCurrentMonth()
        assertThat(viewModel.uiState.value.month).isEqualTo(thisMonth)
    }

    @Test
    fun noExpenses_disablesPrevious_andListsEveryCategoryAtZero() = runTest {
        collectState()
        expensesLocalRepository.sendExpenses(emptyList())

        val state = viewModel.uiState.value
        assertThat(state.isPreviousEnabled).isFalse()
        assertThat(state.total).isEqualTo(0.0)
        assertThat(state.spending.map { it.change }.distinct()).containsExactly(SpendingChange.None)
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
    }
}
