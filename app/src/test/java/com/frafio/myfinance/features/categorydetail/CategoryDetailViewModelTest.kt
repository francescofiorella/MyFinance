package com.frafio.myfinance.features.categorydetail

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.ExpenseCategories
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

class CategoryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesLocalRepository = TestExpensesLocalRepository()
    private val pendingExpensesFilter = PendingExpensesFilter()

    private val dining = CATEGORIES.DINING.value
    private val groceries = CATEGORIES.GROCERIES.value
    private val may2024 = LocalDate.of(2024, 5, 1)

    private lateinit var viewModel: CategoryDetailViewModel

    @Before
    fun setup() {
        viewModel = CategoryDetailViewModel(
            expensesLocalRepository,
            pendingExpensesFilter,
            UnconfinedTestDispatcher(),
            RootKey.CategoryDetail(categoryId = dining, year = 2024, month = 5)
        )
    }

    @Test
    fun exposesTheCategoryAndMonthOfTheKey_andNoStatsUntilLoaded() {
        assertThat(viewModel.category).isEqualTo(ExpenseCategories.get(dining))
        assertThat(viewModel.month).isEqualTo(may2024)
        assertThat(viewModel.stats.value).isNull()
    }

    @Test
    fun stats_useTheCategorysTwelveMonthWindow_andTheShareOfTheWholeMonth() = runTest {
        collectStats()

        expensesLocalRepository.sendExpenses(
            testExpense(name = "Sushi", price = 30.0, date = may2024, category = dining),
            testExpense(name = "Sushi", price = 20.0, date = LocalDate.of(2023, 6, 3), category = dining),
            // Outside the window: June 2023 is the first month in it.
            testExpense(name = "Sushi", price = 1000.0, date = LocalDate.of(2023, 5, 31), category = dining),
            // Another category: only counts toward the month's total.
            testExpense(name = "Market", price = 90.0, date = may2024, category = groceries),
        )

        val stats = viewModel.stats.value!!
        assertThat(stats.monthTotal).isEqualTo(30.0)
        assertThat(stats.share).isEqualTo(0.25)
        assertThat(stats.trend.sumOf { it.value }).isEqualTo(50.0)
        assertThat(stats.frequent).containsExactly(FrequentExpense("Sushi", 2, 50.0))
    }

    @Test
    fun onSeeAllExpenses_requestsTheCategoryFilter() {
        viewModel.onSeeAllExpenses()

        assertThat(pendingExpensesFilter.categories.value).containsExactly(dining)
    }

    private fun TestScope.collectStats() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.stats.collect() }
    }
}
