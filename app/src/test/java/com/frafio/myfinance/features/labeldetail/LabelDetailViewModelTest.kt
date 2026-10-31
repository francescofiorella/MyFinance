package com.frafio.myfinance.features.labeldetail

import com.frafio.myfinance.core.data.repository.PendingExpensesFilter
import com.frafio.myfinance.core.navigation.RootKey
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

class LabelDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesLocalRepository = TestExpensesLocalRepository()
    private val pendingExpensesFilter = PendingExpensesFilter()
    private lateinit var viewModel: LabelDetailViewModel

    @Before
    fun setup() {
        viewModel = LabelDetailViewModel(expensesLocalRepository, pendingExpensesFilter, UnconfinedTestDispatcher(), RootKey.LabelDetail("Japan"))
    }

    @Test
    fun exposesTheLabelOfTheKey_andIsLoadingUntilTheExpensesArrive() = runTest {
        collectState()

        assertThat(viewModel.label).isEqualTo("Japan")
        assertThat(viewModel.uiState.value).isEqualTo(LabelDetailUiState.Loading)
    }

    @Test
    fun stats_coverOnlyTheExpensesWithExactlyThatLabel() = runTest {
        collectState()

        expensesLocalRepository.sendExpenses(
            testExpense(name = "Flight", price = 900.0, date = LocalDate.of(2025, 2, 3), labels = listOf("Japan")),
            testExpense(name = "Dinner", price = 50.0, date = LocalDate.of(2025, 8, 13), labels = listOf("Work", "Japan")),
            testExpense(name = "Hotel", price = 400.0, date = LocalDate.of(2024, 5, 1), labels = listOf("Japan 2024")),
            testExpense(name = "Coffee", price = 2.0, date = LocalDate.of(2025, 8, 13))
        )

        val state = viewModel.uiState.value as LabelDetailUiState.Loaded
        assertThat(state.stats.total).isEqualTo(950.0)
        assertThat(state.stats.count).isEqualTo(2)
    }

    @Test
    fun noExpensesWithTheLabel_isEmpty() = runTest {
        collectState()

        expensesLocalRepository.sendExpenses(testExpense(labels = listOf("Work")))

        assertThat(viewModel.uiState.value).isEqualTo(LabelDetailUiState.Empty)
    }

    @Test
    fun onSeeAllExpenses_requestsTheLabelFilter() {
        viewModel.onSeeAllExpenses()

        assertThat(pendingExpensesFilter.labels.value).containsExactly("Japan")
        assertThat(pendingExpensesFilter.categories.value).isNull()
    }

    private fun TestScope.collectState() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
    }
}
