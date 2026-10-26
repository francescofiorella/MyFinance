package com.frafio.myfinance.features.categorydetail

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.repository.PendingExpensesFilter
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.StateFlow
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

    @Test
    fun trend_coversTheLongestRange_endingWithTheMonth() = runTest {
        collect(viewModel.trend)

        expensesLocalRepository.sendExpenses(
            testExpense(name = "Sushi", price = 30.0, date = may2024, category = dining),
            // June 2022 is the first of the 24 months.
            testExpense(name = "Sushi", price = 5.0, date = LocalDate.of(2022, 6, 10), category = dining),
            testExpense(name = "Sushi", price = 1000.0, date = LocalDate.of(2022, 5, 31), category = dining),
            testExpense(name = "Market", price = 90.0, date = may2024, category = groceries),
        )

        val trend = viewModel.trend.value
        assertThat(trend).hasSize(24)
        assertThat(trend.first().year to trend.first().month).isEqualTo(2022 to 6)
        assertThat(trend.last().year to trend.last().month).isEqualTo(2024 to 5)
        assertThat(trend.first().value).isEqualTo(5.0)
        assertThat(trend.last().value).isEqualTo(30.0)
        assertThat(trend.sumOf { it.value }).isEqualTo(35.0)
    }

    @Test
    fun trend_loadsMoreMonths_whenMoreBarsFit() = runTest {
        collect(viewModel.trend)
        expensesLocalRepository.sendExpenses(emptyList())

        viewModel.setTrendVisibleItems(30)

        assertThat(viewModel.trend.value).hasSize(30)
    }

    @Test
    fun trendShownCount_isWhatFits_onAuto_andTheRangeOtherwise() = runTest {
        collect(viewModel.trendShownCount)
        assertThat(viewModel.trendRange.value).isEqualTo(0)
        assertThat(viewModel.trendShownCount.value).isEqualTo(6)

        viewModel.setTrendVisibleItems(9)
        assertThat(viewModel.trendShownCount.value).isEqualTo(9)

        viewModel.selectTrendRange(2)
        assertThat(viewModel.trendShownCount.value).isEqualTo(12)
    }

    @Test
    fun trendTrackingStart_isTheEarliestExpenseOfAnyCategory() = runTest {
        collect(viewModel.trendTrackingStart)
        assertThat(viewModel.trendTrackingStart.value).isNull()

        expensesLocalRepository.sendExpenses(
            testExpense(date = LocalDate.of(2024, 3, 2), category = dining),
            testExpense(date = LocalDate.of(2023, 10, 20), category = groceries),
        )

        assertThat(viewModel.trendTrackingStart.value).isEqualTo(LocalDate.of(2023, 10, 1))
    }

    @Test
    fun trendTrackingStart_isNeverAfterTheMonth() = runTest {
        collect(viewModel.trendTrackingStart)

        expensesLocalRepository.sendExpenses(emptyList())
        assertThat(viewModel.trendTrackingStart.value).isEqualTo(may2024)

        expensesLocalRepository.sendExpenses(testExpense(date = LocalDate.of(2024, 9, 1), category = dining))
        assertThat(viewModel.trendTrackingStart.value).isEqualTo(may2024)
    }

    @Test
    fun trendAvailableRanges_countTheMonthsTrackedUpToTheMonth() = runTest {
        collect(viewModel.trendAvailableRanges)
        assertThat(viewModel.trendAvailableRanges.value).containsExactly(0, 1, 2, 3).inOrder()

        // August 2023 to May 2024 is ten months, whatever the category; later expenses don't count.
        expensesLocalRepository.sendExpenses(
            testExpense(date = LocalDate.of(2023, 8, 4), category = groceries),
            testExpense(date = LocalDate.of(2025, 1, 4), category = dining),
        )

        assertThat(viewModel.trendAvailableRanges.value).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun trendShownCount_unavailableRange_showsAuto() = runTest {
        collect(viewModel.trendShownCount, viewModel.trendAvailableRanges)
        expensesLocalRepository.sendExpenses(testExpense(date = LocalDate.of(2024, 3, 4), category = dining))

        viewModel.selectTrendRange(2)

        assertThat(viewModel.trendShownCount.value).isEqualTo(6)
    }

    @Test
    fun selectTrendRange_outOfBounds_isClamped() {
        viewModel.selectTrendRange(7)

        assertThat(viewModel.trendRange.value).isEqualTo(3)
    }

    private fun TestScope.collectStats() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.stats.collect() }
    }

    private fun TestScope.collect(vararg flows: StateFlow<*>) {
        flows.forEach { flow ->
            backgroundScope.launch(UnconfinedTestDispatcher()) { flow.collect() }
        }
    }
}
