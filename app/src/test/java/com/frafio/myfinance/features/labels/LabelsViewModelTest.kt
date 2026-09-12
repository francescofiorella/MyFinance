package com.frafio.myfinance.features.labels

import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.model.DeleteLabelResult
import com.frafio.myfinance.core.data.model.FinanceResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
class LabelsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesRepository = TestExpensesRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: LabelsViewModel

    @Before
    fun setup() {
        userPreferencesRepository.setLabels(listOf("food", "travel"))
        viewModel = LabelsViewModel(expensesRepository, loadingRepository, userPreferencesRepository)
    }

    @Test
    fun allLabels_isInitialisedFromPreferences() {
        assertThat(viewModel.allLabels.value).containsExactly("food", "travel").inOrder()
    }

    @Test
    fun allLabels_followsPreferences() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.allLabels.collect() }

        userPreferencesRepository.setLabels(listOf("new"))

        assertThat(viewModel.allLabels.value).containsExactly("new")
    }

    @Test
    fun editingLabel_isSetAndCleared() {
        viewModel.setEditingLabel("food")
        assertThat(viewModel.editingLabel.value).isEqualTo("food")

        viewModel.setEditingLabel(null)
        assertThat(viewModel.editingLabel.value).isNull()
    }

    @Test
    fun addLabel_alwaysEmitsSnackBar() = runTest {
        val events = collectEvents()

        viewModel.addLabel("gift")
        expensesRepository.addLabelResult = FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        viewModel.addLabel("again")

        assertThat(expensesRepository.addedLabels).containsExactly("gift", "again").inOrder()
        assertThat(events).containsExactly(
            LabelsUiEvent.ShowSnackBar(FinanceCode.LABEL_ADD_SUCCESS.message),
            LabelsUiEvent.ShowSnackBar(FinanceCode.LABELS_UPDATE_FAILURE.message),
        ).inOrder()
    }

    @Test
    fun deleteLabel_success_emitsLabelDeletedWithAffectedExpenses() = runTest {
        val affected = listOf(testExpense(name = "A", labels = listOf("food")))
        expensesRepository.deleteLabelResult =
            DeleteLabelResult(FinanceResult(FinanceCode.LABEL_DELETE_SUCCESS), affected)
        val events = collectEvents()

        viewModel.deleteLabel("food")

        assertThat(expensesRepository.deletedLabels).containsExactly("food")
        assertThat(events).containsExactly(
            LabelsUiEvent.LabelDeleted("food", affected, FinanceCode.LABEL_DELETE_SUCCESS.message)
        )
    }

    @Test
    fun deleteLabel_failure_emitsSnackBar() = runTest {
        expensesRepository.deleteLabelResult =
            DeleteLabelResult(FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE))
        val events = collectEvents()

        viewModel.deleteLabel("food")

        assertThat(events).containsExactly(
            LabelsUiEvent.ShowSnackBar(FinanceCode.LABELS_UPDATE_FAILURE.message)
        )
    }

    @Test
    fun undoDeleteLabel_runsOnTheCallerScopeAndStaysSilentOnSuccess() = runTest {
        val affected = listOf(testExpense(name = "A"))
        val events = collectEvents()

        viewModel.undoDeleteLabel(backgroundScope, "food", affected)
        advanceUntilIdle()

        assertThat(expensesRepository.undoneLabels).containsExactly("food" to affected)
        assertThat(events).isEmpty()
    }

    @Test
    fun undoDeleteLabel_failure_emitsSnackBar() = runTest {
        expensesRepository.undoDeleteLabelResult = FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        val events = collectEvents()

        viewModel.undoDeleteLabel(backgroundScope, "food", emptyList())
        advanceUntilIdle()

        assertThat(events).containsExactly(
            LabelsUiEvent.ShowSnackBar(FinanceCode.LABELS_UPDATE_FAILURE.message)
        )
    }

    @Test
    fun editLabel_alwaysEmitsSnackBar() = runTest {
        val events = collectEvents()

        viewModel.editLabel("food", "groceries")
        expensesRepository.editLabelResult = FinanceResult(FinanceCode.LABELS_UPDATE_FAILURE)
        viewModel.editLabel("travel", "trips")

        assertThat(expensesRepository.editedLabels)
            .containsExactly("food" to "groceries", "travel" to "trips").inOrder()
        assertThat(events).containsExactly(
            LabelsUiEvent.ShowSnackBar(FinanceCode.LABEL_UPDATE_SUCCESS.message),
            LabelsUiEvent.ShowSnackBar(FinanceCode.LABELS_UPDATE_FAILURE.message),
        ).inOrder()
    }

    @Test
    fun everyOperation_startsAndStopsLoading() = runTest {
        val loading = collectLoading()
        collectEvents()

        viewModel.addLabel("a")
        viewModel.deleteLabel("a")
        viewModel.editLabel("a", "b")
        viewModel.undoDeleteLabel(backgroundScope, "a", emptyList())
        advanceUntilIdle()

        assertThat(loading).containsExactly(false, true, false, true, false, true, false, true, false).inOrder()
    }

    private fun TestScope.collectEvents(): List<LabelsUiEvent> {
        val events = mutableListOf<LabelsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiEvents.toList(events) }
        return events
    }

    private fun TestScope.collectLoading(): List<Boolean> {
        val states = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { loadingRepository.isLoading.toList(states) }
        return states
    }
}
