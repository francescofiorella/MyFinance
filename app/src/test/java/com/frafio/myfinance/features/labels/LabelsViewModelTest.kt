package com.frafio.myfinance.features.labels

import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
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

class LabelsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val expensesLocalRepository = TestExpensesLocalRepository()
    private lateinit var viewModel: LabelsViewModel

    @Before
    fun setup() {
        viewModel = LabelsViewModel(userPreferencesRepository, expensesLocalRepository, UnconfinedTestDispatcher())
    }

    @Test
    fun groups_areNullUntilTheExpensesLoad() = runTest {
        collectGroups()

        assertThat(viewModel.groups.value).isNull()
    }

    @Test
    fun groups_summariseTheLabelledExpenses_ofTheLabelsInThePreferences() = runTest {
        collectGroups()
        userPreferencesRepository.setLabels(listOf("Japan", "Paris"))

        expensesLocalRepository.sendExpenses(
            testExpense(name = "Flight", price = 900.0, date = LocalDate.of(2025, 2, 3), labels = listOf("Japan")),
            testExpense(name = "Hotel", price = 300.0, date = LocalDate.of(2025, 8, 12), labels = listOf("Japan")),
            testExpense(name = "Coffee", price = 2.0, date = LocalDate.of(2025, 8, 12))
        )

        assertThat(viewModel.groups.value).containsExactly(
            LabelGroup(2025, listOf(LabelSummary("Japan", 1200.0, 2, LocalDate.of(2025, 2, 3), LocalDate.of(2025, 8, 12)))),
            LabelGroup(null, listOf(LabelSummary("Paris", 0.0, 0, null, null)))
        ).inOrder()
    }

    @Test
    fun groups_followLabelChanges() = runTest {
        collectGroups()
        userPreferencesRepository.setLabels(listOf("Japan"))
        expensesLocalRepository.sendExpenses(emptyList())

        userPreferencesRepository.setLabels(emptyList())

        assertThat(viewModel.groups.value).isEmpty()
    }

    private fun TestScope.collectGroups() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.groups.collect() }
    }
}
