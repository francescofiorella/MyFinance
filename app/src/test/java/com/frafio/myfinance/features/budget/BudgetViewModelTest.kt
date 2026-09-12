package com.frafio.myfinance.features.budget

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestIncomeRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class BudgetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesRepository = TestExpensesRepository()
    private val incomeRepository = TestIncomeRepository()
    private val incomesLocalRepository = TestIncomesLocalRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: BudgetViewModel

    // The incomes flow calls addTotalsToIncomes with the real today, so fixtures stay in this year.
    private val thisYear = LocalDate.now().year
    private val totalCategory = FirestoreEnums.CATEGORIES.TOTAL.value

    @Before
    fun setup() {
        userPreferencesRepository.setMonthlyBudget(100.0)
        viewModel = BudgetViewModel(
            expensesRepository,
            incomeRepository,
            incomesLocalRepository,
            userPreferencesRepository,
            loadingRepository,
            UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun budgets_areInitialisedFromPreferences() {
        assertThat(viewModel.monthlyBudget.value).isEqualTo(100.0)
        assertThat(viewModel.annualBudget.value).isEqualTo(1200.0)
    }

    @Test
    fun annualBudget_isTwelveTimesMonthly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.monthlyBudget.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.annualBudget.collect() }

        userPreferencesRepository.setMonthlyBudget(250.0)

        assertThat(viewModel.monthlyBudget.value).isEqualTo(250.0)
        assertThat(viewModel.annualBudget.value).isEqualTo(3000.0)
    }

    @Test
    fun incomes_isEmptyUntilCollected() {
        incomesLocalRepository.sendIncomes(testIncome(date = LocalDate.of(thisYear, 1, 1)))
        assertThat(viewModel.incomes.value).isEmpty()
    }

    @Test
    fun incomes_areGroupedWithYearTotals() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.incomes.collect() }
        val jan = testIncome(name = "Jan", price = 10.0, date = LocalDate.of(thisYear, 1, 1))
        val feb = testIncome(name = "Feb", price = 20.0, date = LocalDate.of(thisYear, 2, 1))

        incomesLocalRepository.sendIncomes(jan, feb)

        val incomes = viewModel.incomes.value
        assertThat(incomes).hasSize(3)
        assertThat(incomes[0].category).isEqualTo(totalCategory)
        assertThat(incomes[0].price).isEqualTo(30.0)
        // Local repository orders newest first.
        assertThat(incomes.drop(1)).containsExactly(feb, jan).inOrder()
    }

    @Test
    fun incomes_areLimitedToOneHundredUntilLoadMore() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.incomes.collect() }
        val many = (1..150).map { testIncome(name = "I$it", price = 1.0, date = LocalDate.of(thisYear, 1, 1)) }

        incomesLocalRepository.sendIncomes(many)
        assertThat(viewModel.incomes.value.filter { it.category != totalCategory }).hasSize(100)

        viewModel.loadMore()
        assertThat(viewModel.incomes.value.filter { it.category != totalCategory }).hasSize(150)
    }

    @Test
    fun isIncomesEmpty_isNullDuringFirstSyncWithNoData() = runTest {
        assertThat(viewModel.isIncomesEmpty.value).isNull()

        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isIncomesEmpty.collect() }
        incomesLocalRepository.sendIncomes(emptyList())

        assertThat(viewModel.isIncomesEmpty.value).isNull()
    }

    @Test
    fun isIncomesEmpty_isTrueAfterFirstSyncWithNoData() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isIncomesEmpty.collect() }
        incomesLocalRepository.sendIncomes(emptyList())

        loadingRepository.stopFirstSync()

        assertThat(viewModel.isIncomesEmpty.value).isTrue()
    }

    @Test
    fun isIncomesEmpty_isFalseAsSoonAsThereIsData() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isIncomesEmpty.collect() }

        incomesLocalRepository.sendIncomes(testIncome())

        assertThat(viewModel.isIncomesEmpty.value).isFalse()
    }

    @Test
    fun isIncomesEmpty_initialValueIsFalseWhenNotFirstSync() {
        loadingRepository.stopFirstSync()
        viewModel = BudgetViewModel(
            expensesRepository, incomeRepository, incomesLocalRepository, userPreferencesRepository,
            loadingRepository, UnconfinedTestDispatcher(),
        )
        assertThat(viewModel.isIncomesEmpty.value).isFalse()
    }

    @Test
    fun itemMetadata_groupsByYearAndIsolatesTotals() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.incomes.collect() }
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.itemMetadata.collect() }
        val incomes = listOf(
            testIncome(name = "A", price = 1.0, date = LocalDate.of(thisYear, 3, 1)),
            testIncome(name = "B", price = 1.0, date = LocalDate.of(thisYear, 2, 1)),
            testIncome(name = "C", price = 1.0, date = LocalDate.of(thisYear, 1, 1)),
            testIncome(name = "D", price = 1.0, date = LocalDate.of(thisYear - 1, 6, 1)),
        )

        incomesLocalRepository.sendIncomes(incomes)

        // [TOTAL(thisYear), A, B, C, TOTAL(lastYear), D]
        assertThat(viewModel.itemMetadata.value).containsExactly(
            0, 0 to 1,
            1, 0 to 3,
            2, 1 to 3,
            3, 2 to 3,
            4, 0 to 1,
            5, 0 to 1,
        )
    }

    @Test
    fun setMonthlyBudget_success_emitsThePreviousBudgetForUndo() = runTest {
        val events = collectEvents()

        viewModel.setMonthlyBudget(500.0)

        assertThat(expensesRepository.budgetCalls).containsExactly(500.0)
        assertThat(events).containsExactly(BudgetUiEvent.BudgetUpdated(100.0))
    }

    @Test
    fun setMonthlyBudget_failure_emitsSnackBar() = runTest {
        expensesRepository.budgetResult = FinanceResult(FinanceCode.BUDGET_UPDATE_FAILURE)
        val events = collectEvents()

        viewModel.setMonthlyBudget(500.0)

        assertThat(events).containsExactly(BudgetUiEvent.ShowSnackBar(FinanceCode.BUDGET_UPDATE_FAILURE.message))
    }

    @Test
    fun setMonthlyBudget_withoutNotify_callsTheRepositoryButEmitsNothing() = runTest {
        val events = collectEvents()

        viewModel.setMonthlyBudget(500.0, notify = false)

        assertThat(expensesRepository.budgetCalls).containsExactly(500.0)
        assertThat(events).isEmpty()
    }

    @Test
    fun deleteMonthlyBudget_setsItToZero() = runTest {
        val events = collectEvents()

        viewModel.deleteMonthlyBudget()

        assertThat(expensesRepository.budgetCalls).containsExactly(0.0)
        assertThat(events).containsExactly(BudgetUiEvent.BudgetUpdated(100.0))
    }

    @Test
    fun deleteIncome_success_emitsIncomeDeleted() = runTest {
        val income = testIncome()
        val events = collectEvents()

        viewModel.deleteIncome(income)

        assertThat(incomeRepository.deletedIncomes).containsExactly(income)
        assertThat(events).containsExactly(BudgetUiEvent.IncomeDeleted(income))
    }

    @Test
    fun deleteIncome_failure_emitsSnackBar() = runTest {
        incomeRepository.deleteResult = FinanceResult(FinanceCode.INCOME_DELETE_FAILURE)
        val events = collectEvents()

        viewModel.deleteIncome(testIncome())

        assertThat(events).containsExactly(BudgetUiEvent.ShowSnackBar(FinanceCode.INCOME_DELETE_FAILURE.message))
    }

    @Test
    fun undoDeleteIncome_reEditsAndStaysSilentOnSuccess() = runTest {
        val income = testIncome()
        val events = collectEvents()

        viewModel.undoDeleteIncome(income)

        assertThat(incomeRepository.editedIncomes).containsExactly(income)
        assertThat(events).isEmpty()
    }

    @Test
    fun undoDeleteIncome_failure_emitsSnackBar() = runTest {
        incomeRepository.editResult = FinanceResult(FinanceCode.INCOME_EDIT_FAILURE)
        val events = collectEvents()

        viewModel.undoDeleteIncome(testIncome())

        assertThat(events).containsExactly(BudgetUiEvent.ShowSnackBar(FinanceCode.INCOME_EDIT_FAILURE.message))
    }

    @Test
    fun everyWrite_startsAndStopsLoading() = runTest {
        useStandardMain()
        val loading = collectLoading()
        collectEvents()

        viewModel.setMonthlyBudget(1.0)
        viewModel.deleteIncome(testIncome())
        viewModel.undoDeleteIncome(testIncome())
        runCurrent()

        assertThat(loading).containsExactly(false, true, false, true, false, true, false).inOrder()
    }

    @Test
    fun scrollToId_replaysToLateSubscribers() = runTest {
        viewModel.scrollToId("2024")

        val ids = mutableListOf<String?>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.scrollToId.toList(ids) }

        assertThat(ids).containsExactly("2024")
    }

    private fun TestScope.collectEvents(): List<BudgetUiEvent> {
        val events = mutableListOf<BudgetUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiEvents.toList(events) }
        return events
    }

    // When nothing suspends between startLoading and stopLoading, an unconfined Main lets the
    // StateFlow conflate the `true` away before the collector runs. A standard Main plus
    // runCurrent() observes both steps.
    private fun TestScope.useStandardMain() = Dispatchers.setMain(StandardTestDispatcher(testScheduler))

    private fun TestScope.collectLoading(): List<Boolean> {
        val states = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { loadingRepository.isLoading.toList(states) }
        return states
    }
}
