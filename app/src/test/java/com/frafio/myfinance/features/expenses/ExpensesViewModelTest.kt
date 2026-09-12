package com.frafio.myfinance.features.expenses

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class ExpensesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesRepository = TestExpensesRepository()
    private val expensesLocalRepository = TestExpensesLocalRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: ExpensesViewModel

    // The expenses flow calls addTotalsToExpenses with the real today, so fixtures are relative to it.
    private val today: LocalDate = LocalDate.now()
    private val yesterday: LocalDate = today.minusDays(1)
    private val totalCategory = FirestoreEnums.CATEGORIES.TOTAL.value
    private val jollyCategory = FirestoreEnums.CATEGORIES.JOLLY.value
    private val spendingCategories = (0..8).toList()

    @Before
    fun setup() {
        viewModel = ExpensesViewModel(
            expensesRepository,
            expensesLocalRepository,
            userPreferencesRepository,
            loadingRepository,
            UnconfinedTestDispatcher(),
        )
    }

    // region filters

    @Test
    fun defaultFilters_queryEverySpendingCategoryWithAnEmptyName() = runTest {
        collectExpenses()

        val call = expensesLocalRepository.filterCalls.single()
        assertThat(call.name).isEmpty()
        assertThat(call.categories).containsExactlyElementsIn(spendingCategories).inOrder()
        assertThat(call.firstTimestamp).isNull()
    }

    @Test
    fun onSearchQueryChanged_trimsBeforeQuerying() = runTest {
        collectExpenses()

        viewModel.onSearchQueryChanged("  latte ")

        assertThat(viewModel.searchQuery.value).isEqualTo("  latte ")
        assertThat(expensesLocalRepository.filterCalls.last().name).isEqualTo("latte")
    }

    @Test
    fun onCategoryFilterChanged_togglesAndAccumulates() = runTest {
        collectExpenses()

        viewModel.onCategoryFilterChanged(1)
        viewModel.onCategoryFilterChanged(3)
        assertThat(viewModel.selectedCategories.value).containsExactly(1, 3).inOrder()
        assertThat(expensesLocalRepository.filterCalls.last().categories).containsExactly(1, 3).inOrder()

        viewModel.onCategoryFilterChanged(1)
        assertThat(viewModel.selectedCategories.value).containsExactly(3)
        assertThat(expensesLocalRepository.filterCalls.last().categories).containsExactly(3)
    }

    @Test
    fun onLabelFilterChanged_isIdempotentAndRemovable() {
        viewModel.onLabelFilterChanged("a", true)
        viewModel.onLabelFilterChanged("a", true)
        viewModel.onLabelFilterChanged("b", true)
        assertThat(viewModel.selectedLabels.value).containsExactly("a", "b").inOrder()

        viewModel.onLabelFilterChanged("a", false)
        assertThat(viewModel.selectedLabels.value).containsExactly("b")
    }

    @Test
    fun labelFilter_isAppliedInMemoryAfterTheQuery() = runTest {
        collectExpenses()
        val tagged = testExpense(name = "Tagged", labels = listOf("work"), date = yesterday)
        val untagged = testExpense(name = "Untagged", date = yesterday)
        expensesLocalRepository.sendExpenses(tagged, untagged)

        viewModel.onLabelFilterChanged("work", true)

        assertThat(expensesLocalRepository.filterCalls.last().name).isEmpty()
        assertThat(viewModel.expenses.value.filter { it.isRealExpense() }).containsExactly(tagged)
    }

    @Test
    fun onDateFilterChanged_queriesAnInclusiveStartAndExclusiveEndTheDayAfter() = runTest {
        collectExpenses()
        val from = LocalDate.of(2024, 3, 1)
        val to = LocalDate.of(2024, 3, 31)

        viewModel.onDateFilterChanged(from to to)

        val call = expensesLocalRepository.filterCalls.last()
        assertThat(call.firstTimestamp).isEqualTo(dateToUTCTimestamp(from))
        assertThat(call.lastTimestamp).isEqualTo(dateToUTCTimestamp(LocalDate.of(2024, 4, 1)))
        assertThat(viewModel.dateRange.value).isEqualTo(from to to)
    }

    @Test
    fun clearingTheDateFilter_goesBackToTheUndatedQuery() = runTest {
        collectExpenses()
        viewModel.onDateFilterChanged(LocalDate.of(2024, 3, 1) to LocalDate.of(2024, 3, 31))

        viewModel.onDateFilterChanged(null)

        assertThat(expensesLocalRepository.filterCalls.last().firstTimestamp).isNull()
    }

    @Test
    fun labels_mirrorPreferences() = runTest {
        userPreferencesRepository.setLabels(listOf("x"))
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.labels.collect() }
        assertThat(viewModel.labels.value).containsExactly("x")
    }

    // endregion

    // region expenses list

    @Test
    fun expenses_isEmptyUntilCollected() {
        expensesLocalRepository.sendExpenses(testExpense(date = today))
        assertThat(viewModel.expenses.value).isEmpty()
    }

    @Test
    fun expenses_withoutFilters_includeTheTodayBlock() = runTest {
        collectExpenses()
        val old = testExpense(name = "Old", date = yesterday)

        expensesLocalRepository.sendExpenses(old)

        val expenses = viewModel.expenses.value
        assertThat(expenses.map { it.category }).containsExactly(totalCategory, jollyCategory, totalCategory, old.category).inOrder()
        assertThat(expenses[3]).isEqualTo(old)
    }

    @Test
    fun expenses_withAnyFilter_omitTheTodayBlock() = runTest {
        collectExpenses()
        val old = testExpense(name = "Old", date = yesterday)
        expensesLocalRepository.sendExpenses(old)

        viewModel.onSearchQueryChanged("Old")

        val expenses = viewModel.expenses.value
        assertThat(expenses.map { it.category }).containsExactly(totalCategory, old.category).inOrder()
        assertThat(expenses.none { it.category == jollyCategory }).isTrue()
    }

    @Test
    fun expenses_areLimitedToFiftyUntilLoadMore() = runTest {
        collectExpenses()
        val many = (1..70).map { testExpense(name = "E$it", date = today) }

        expensesLocalRepository.sendExpenses(many)
        assertThat(viewModel.expenses.value.filter { it.isRealExpense() }).hasSize(50)

        viewModel.loadMore()
        assertThat(viewModel.expenses.value.filter { it.isRealExpense() }).hasSize(70)
    }

    @Test
    fun totalFilteredExpenses_sumsTheWholeFilteredListNotJustThePage() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.totalFilteredExpenses.collect() }
        val many = (1..70).map { testExpense(name = "E$it", price = 1.0, date = today) }

        expensesLocalRepository.sendExpenses(many)

        assertThat(viewModel.totalFilteredExpenses.value).isEqualTo(70.0)
    }

    @Test
    fun totalFilteredExpenses_treatsNullPriceAsZero() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.totalFilteredExpenses.collect() }

        expensesLocalRepository.sendExpenses(
            testExpense(name = "A", price = 2.5, date = today),
            testExpense(name = "B", price = null, date = today),
        )

        assertThat(viewModel.totalFilteredExpenses.value).isEqualTo(2.5)
    }

    @Test
    fun isExpensesEmpty_isNullDuringFirstSyncAndTrueAfterWithNoData() = runTest {
        assertThat(viewModel.isExpensesEmpty.value).isNull()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.isExpensesEmpty.collect() }
        expensesLocalRepository.sendExpenses(emptyList())
        assertThat(viewModel.isExpensesEmpty.value).isNull()

        loadingRepository.stopFirstSync()
        assertThat(viewModel.isExpensesEmpty.value).isTrue()

        expensesLocalRepository.sendExpenses(testExpense())
        assertThat(viewModel.isExpensesEmpty.value).isFalse()
    }

    @Test
    fun itemMetadata_groupsByDayAndIsolatesTotalAndJollyRows() = runTest {
        collectExpenses()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.itemMetadata.collect() }
        val twoDaysAgo = today.minusDays(2)

        expensesLocalRepository.sendExpenses(
            testExpense(name = "A", price = 3.0, date = yesterday),
            testExpense(name = "B", price = 2.0, date = yesterday),
            testExpense(name = "C", price = 1.0, date = yesterday),
            testExpense(name = "D", price = 1.0, date = twoDaysAgo),
        )

        // [TOTAL(today), JOLLY, TOTAL(yesterday), A, B, C, TOTAL(twoDaysAgo), D]
        assertThat(viewModel.itemMetadata.value).containsExactly(
            0, 0 to 1,
            1, 0 to 1,
            2, 0 to 1,
            3, 0 to 3,
            4, 1 to 3,
            5, 2 to 3,
            6, 0 to 1,
            7, 0 to 1,
        )
    }

    @Test
    fun scrollToId_replaysToLateSubscribers() = runTest {
        viewModel.scrollToId("total_1_1_2024")

        val ids = mutableListOf<String?>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.scrollToId.toList(ids) }

        assertThat(ids).containsExactly("total_1_1_2024")
    }

    // endregion

    // region writes

    @Test
    fun setEditingExpense_setsAndClears() {
        val expense = testExpense()
        viewModel.setEditingExpense(expense)
        assertThat(viewModel.editingExpense.value).isEqualTo(expense)
        viewModel.setEditingExpense(null)
        assertThat(viewModel.editingExpense.value).isNull()
    }

    @Test
    fun deleteExpense_success_emitsExpenseDeleted() = runTest {
        val expense = testExpense()
        val events = collectEvents()

        viewModel.deleteExpense(expense)

        assertThat(expensesRepository.deletedExpenses).containsExactly(expense)
        assertThat(events).containsExactly(ExpensesUiEvent.ExpenseDeleted(expense))
    }

    @Test
    fun deleteExpense_failure_emitsSnackBar() = runTest {
        expensesRepository.deleteResult = FinanceResult(FinanceCode.EXPENSE_DELETE_FAILURE)
        val events = collectEvents()

        viewModel.deleteExpense(testExpense())

        assertThat(events).containsExactly(ExpensesUiEvent.ShowSnackBar(FinanceCode.EXPENSE_DELETE_FAILURE.message))
    }

    @Test
    fun undoDeleteExpense_reEditsAndStaysSilentOnSuccess() = runTest {
        val expense = testExpense()
        val events = collectEvents()

        viewModel.undoDeleteExpense(expense)

        assertThat(expensesRepository.editedExpenses).containsExactly(expense)
        assertThat(events).isEmpty()
    }

    @Test
    fun undoDeleteExpense_failure_emitsSnackBar() = runTest {
        expensesRepository.editResult = FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
        val events = collectEvents()

        viewModel.undoDeleteExpense(testExpense())

        assertThat(events).containsExactly(ExpensesUiEvent.ShowSnackBar(FinanceCode.EXPENSE_EDIT_FAILURE.message))
    }

    @Test
    fun updateCategory_recomputesTheTimestampAndRefreshesTheEditingExpense() = runTest {
        val expense = testExpense(date = LocalDate.of(2024, 5, 20), category = 1).copy(timestamp = 0L)
        viewModel.setEditingExpense(expense)
        collectEvents()

        viewModel.updateCategory(expense, 7)

        val edited = expensesRepository.editedExpenses.single()
        assertThat(edited.category).isEqualTo(7)
        assertThat(edited.timestamp).isEqualTo(dateToUTCTimestamp(2024, 5, 20))
        assertThat(edited.id).isEqualTo(expense.id)
        assertThat(viewModel.editingExpense.value).isEqualTo(edited)
    }

    @Test
    fun updateCategory_doesNotTouchAnUnrelatedEditingExpense() = runTest {
        val editing = testExpense(name = "Other", id = "other")
        viewModel.setEditingExpense(editing)
        collectEvents()

        viewModel.updateCategory(testExpense(id = "target"), 7)

        assertThat(viewModel.editingExpense.value).isEqualTo(editing)
    }

    @Test
    fun updateCategory_failure_emitsSnackBar() = runTest {
        expensesRepository.editResult = FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
        val events = collectEvents()

        viewModel.updateCategory(testExpense(), 7)

        assertThat(events).containsExactly(ExpensesUiEvent.ShowSnackBar(FinanceCode.EXPENSE_EDIT_FAILURE.message))
    }

    @Test
    fun addLabelToExpense_trimsAndRefreshesTheEditingExpenseFromTheLocalStore() = runTest {
        val expense = testExpense(id = "e1")
        val refreshed = expense.copy(labels = listOf("work"))
        expensesLocalRepository.sendExpenses(refreshed)
        viewModel.setEditingExpense(expense)
        collectEvents()

        viewModel.addLabelToExpense(expense, "  work ")

        assertThat(expensesRepository.expenseLabelCalls).containsExactly(Triple("e1", "work", true))
        assertThat(viewModel.editingExpense.value).isEqualTo(refreshed)
    }

    @Test
    fun addLabelToExpense_blankOrDuplicate_isANoOp() = runTest {
        val events = collectEvents()
        val expense = testExpense(labels = listOf("work"))

        viewModel.addLabelToExpense(expense, "   ")
        viewModel.addLabelToExpense(expense, "work")

        assertThat(expensesRepository.expenseLabelCalls).isEmpty()
        assertThat(events).isEmpty()
        assertThat(loadingRepository.isLoading.value).isFalse()
    }

    @Test
    fun addLabelToExpense_failure_emitsSnackBar() = runTest {
        expensesRepository.expenseLabelsResult = FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
        val events = collectEvents()

        viewModel.addLabelToExpense(testExpense(), "work")

        assertThat(events).containsExactly(ExpensesUiEvent.ShowSnackBar(FinanceCode.EXPENSE_EDIT_FAILURE.message))
    }

    @Test
    fun removeLabelFromExpense_delegatesAndRefreshesTheEditingExpense() = runTest {
        val expense = testExpense(id = "e1", labels = listOf("work"))
        val refreshed = expense.copy(labels = emptyList())
        expensesLocalRepository.sendExpenses(refreshed)
        viewModel.setEditingExpense(expense)
        collectEvents()

        viewModel.removeLabelFromExpense(expense, "work")

        assertThat(expensesRepository.expenseLabelCalls).containsExactly(Triple("e1", "work", false))
        assertThat(viewModel.editingExpense.value).isEqualTo(refreshed)
    }

    @Test
    fun removeLabelFromExpense_failure_emitsSnackBar() = runTest {
        expensesRepository.expenseLabelsResult = FinanceResult(FinanceCode.EXPENSE_EDIT_FAILURE)
        val events = collectEvents()

        viewModel.removeLabelFromExpense(testExpense(), "work")

        assertThat(events).containsExactly(ExpensesUiEvent.ShowSnackBar(FinanceCode.EXPENSE_EDIT_FAILURE.message))
    }

    @Test
    fun everyWrite_startsAndStopsLoading() = runTest {
        useStandardMain()
        val loading = collectLoading()
        collectEvents()
        val expense = testExpense()

        viewModel.deleteExpense(expense)
        viewModel.undoDeleteExpense(expense)
        viewModel.updateCategory(expense, 1)
        viewModel.addLabelToExpense(expense, "a")
        viewModel.removeLabelFromExpense(expense, "a")
        runCurrent()

        assertThat(loading).containsExactly(
            false, true, false, true, false, true, false, true, false, true, false,
        ).inOrder()
    }

    // endregion

    private fun Expense.isRealExpense() = category != totalCategory && category != jollyCategory

    private fun TestScope.collectExpenses() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.expenses.collect() }
    }

    private fun TestScope.collectEvents(): List<ExpensesUiEvent> {
        val events = mutableListOf<ExpensesUiEvent>()
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
