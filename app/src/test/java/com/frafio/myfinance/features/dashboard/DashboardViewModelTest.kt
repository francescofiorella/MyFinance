package com.frafio.myfinance.features.dashboard

import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 *
 * The ViewModel captures LocalDate.now() at construction, so every date here is relative to it.
 */
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesLocalRepository = TestExpensesLocalRepository()
    private val incomesLocalRepository = TestIncomesLocalRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: DashboardViewModel

    private val today: LocalDate = LocalDate.now()
    private val thisMonth: LocalDate = today.with(TemporalAdjusters.firstDayOfMonth())

    @Before
    fun setup() {
        userPreferencesRepository.setMonthlyBudget(300.0)
        viewModel = DashboardViewModel(
            expensesLocalRepository,
            incomesLocalRepository,
            userPreferencesRepository,
            loadingRepository,
        )
    }

    // region emptiness and sums

    @Test
    fun isListEmpty_initialValueIsNullDuringFirstSync() {
        assertThat(viewModel.isListEmpty.value).isNull()

        loadingRepository.stopFirstSync()
        val later = DashboardViewModel(expensesLocalRepository, incomesLocalRepository, userPreferencesRepository, loadingRepository)
        assertThat(later.isListEmpty.value).isFalse()
    }

    @Test
    fun isListEmpty_isNullThenTrueThenFalse() = runTest {
        collect(viewModel.isListEmpty)
        expensesLocalRepository.sendExpenses(emptyList())
        incomesLocalRepository.sendIncomes(emptyList())
        assertThat(viewModel.isListEmpty.value).isNull()

        loadingRepository.stopFirstSync()
        assertThat(viewModel.isListEmpty.value).isTrue()

        incomesLocalRepository.sendIncomes(testIncome())
        assertThat(viewModel.isListEmpty.value).isFalse()
    }

    @Test
    fun sums_mapMissingValuesToZero() = runTest {
        collect(viewModel.todaySum, viewModel.thisMonthSum, viewModel.thisYearSum)

        expensesLocalRepository.sendExpenses(emptyList())

        assertThat(viewModel.todaySum.value).isEqualTo(0.0)
        assertThat(viewModel.thisMonthSum.value).isEqualTo(0.0)
        assertThat(viewModel.thisYearSum.value).isEqualTo(0.0)
    }

    @Test
    fun sums_coverTodayThisMonthAndThisYear() = runTest {
        collect(viewModel.todaySum, viewModel.thisMonthSum, viewModel.thisYearSum)
        // Use the 15th so "a different day this month" always exists.
        val anchor = thisMonth.plusDays(14)
        val otherDay = if (today == anchor) anchor.minusDays(1) else anchor
        val lastYear = today.minusYears(1)

        expensesLocalRepository.sendExpenses(
            testExpense(name = "A", price = 1.0, date = today),
            testExpense(name = "B", price = 10.0, date = otherDay),
            testExpense(name = "C", price = 100.0, date = lastYear),
        )

        assertThat(viewModel.todaySum.value).isEqualTo(1.0)
        assertThat(viewModel.thisMonthSum.value).isEqualTo(11.0)
        assertThat(viewModel.thisYearSum.value).isEqualTo(11.0)
    }

    @Test
    fun monthlyBudget_mirrorsPreferences() = runTest {
        assertThat(viewModel.monthlyBudget.value).isEqualTo(300.0)

        collect(viewModel.monthlyBudget)
        userPreferencesRepository.setMonthlyBudget(50.0)

        assertThat(viewModel.monthlyBudget.value).isEqualTo(50.0)
    }

    // endregion

    // region earliestFinancialDate

    @Test
    fun earliestFinancialDate_isNullWithoutData() = runTest {
        collect(viewModel.earliestFinancialDate)
        expensesLocalRepository.sendExpenses(emptyList())
        incomesLocalRepository.sendIncomes(emptyList())

        assertThat(viewModel.earliestFinancialDate.value).isNull()
    }

    @Test
    fun earliestFinancialDate_usesWhicheverSideHasData() = runTest {
        collect(viewModel.earliestFinancialDate)

        expensesLocalRepository.sendExpenses(testExpense(date = LocalDate.of(2020, 5, 1)))
        incomesLocalRepository.sendIncomes(emptyList())
        assertThat(viewModel.earliestFinancialDate.value).isEqualTo(DatePoint(2020, 5))

        expensesLocalRepository.sendExpenses(emptyList())
        incomesLocalRepository.sendIncomes(testIncome(date = LocalDate.of(2019, 7, 1)))
        assertThat(viewModel.earliestFinancialDate.value).isEqualTo(DatePoint(2019, 7))
    }

    @Test
    fun earliestFinancialDate_picksTheEarlierYear() = runTest {
        collect(viewModel.earliestFinancialDate)

        expensesLocalRepository.sendExpenses(testExpense(date = LocalDate.of(2018, 12, 1)))
        incomesLocalRepository.sendIncomes(testIncome(date = LocalDate.of(2019, 1, 1)))
        assertThat(viewModel.earliestFinancialDate.value).isEqualTo(DatePoint(2018, 12))

        expensesLocalRepository.sendExpenses(testExpense(date = LocalDate.of(2020, 1, 1)))
        assertThat(viewModel.earliestFinancialDate.value).isEqualTo(DatePoint(2019, 1))
    }

    @Test
    fun earliestFinancialDate_sameYear_picksTheEarlierMonth() = runTest {
        collect(viewModel.earliestFinancialDate)

        expensesLocalRepository.sendExpenses(testExpense(date = LocalDate.of(2020, 3, 1)))
        incomesLocalRepository.sendIncomes(testIncome(date = LocalDate.of(2020, 6, 1)))
        assertThat(viewModel.earliestFinancialDate.value).isEqualTo(DatePoint(2020, 3))
    }

    @Test
    fun earliestFinancialDate_sameYearAndMonth_picksTheIncome() = runTest {
        collect(viewModel.earliestFinancialDate)

        expensesLocalRepository.sendExpenses(testExpense(date = LocalDate.of(2020, 3, 1)))
        incomesLocalRepository.sendIncomes(testIncome(date = LocalDate.of(2020, 3, 15)))

        // Both sides resolve to the same DatePoint; the else branch returns the income's.
        assertThat(viewModel.earliestFinancialDate.value).isEqualTo(DatePoint(2020, 3))
    }

    // endregion

    // region annual balance

    @Test
    fun annualBalanceData_startsOnThisYearAndReQueriesWhenTheYearChanges() = runTest {
        collect(viewModel.annualBalanceData)
        expensesLocalRepository.sendExpenses(
            testExpense(name = "A", price = 5.0, date = today),
            testExpense(name = "B", price = 50.0, date = today.minusYears(1)),
        )
        incomesLocalRepository.sendIncomes(
            testIncome(name = "S", price = 100.0, date = today),
            testIncome(name = "T", price = 1000.0, date = today.minusYears(1)),
        )

        assertThat(viewModel.annualBalanceData.value).isEqualTo(AnnualBalanceData(today.year, 100.0, 5.0))

        viewModel.previousBalanceYear()
        assertThat(viewModel.annualBalanceData.value).isEqualTo(AnnualBalanceData(today.year - 1, 1000.0, 50.0))

        viewModel.todayBalanceYear()
        assertThat(viewModel.annualBalanceData.value.year).isEqualTo(today.year)
    }

    @Test
    fun nextBalanceYear_isClampedToThisYearButPreviousIsNot() = runTest {
        collect(viewModel.annualBalanceData)
        expensesLocalRepository.sendExpenses(emptyList())
        incomesLocalRepository.sendIncomes(emptyList())

        viewModel.nextBalanceYear()
        assertThat(viewModel.annualBalanceData.value.year).isEqualTo(today.year)

        repeat(3) { viewModel.previousBalanceYear() }
        assertThat(viewModel.annualBalanceData.value.year).isEqualTo(today.year - 3)

        viewModel.nextBalanceYear()
        assertThat(viewModel.annualBalanceData.value.year).isEqualTo(today.year - 2)
    }

    @Test
    fun isPreviousBalanceYearEnabled_dependsOnTheEarliestDate() = runTest {
        collect(viewModel.isPreviousBalanceYearEnabled, viewModel.earliestFinancialDate)
        expensesLocalRepository.sendExpenses(emptyList())
        incomesLocalRepository.sendIncomes(emptyList())
        assertThat(viewModel.isPreviousBalanceYearEnabled.value).isFalse()

        expensesLocalRepository.sendExpenses(testExpense(date = today.minusYears(1)))
        assertThat(viewModel.isPreviousBalanceYearEnabled.value).isTrue()

        viewModel.previousBalanceYear()
        assertThat(viewModel.isPreviousBalanceYearEnabled.value).isFalse()
    }

    // endregion

    // region bar chart

    @Test
    fun barChartData_fillsMissingMonthsWithZeroInAscendingOrder() = runTest {
        collect(viewModel.barChartData)
        val twoMonthsAgo = thisMonth.minusMonths(2)
        val fiveMonthsAgo = thisMonth.minusMonths(5)

        expensesLocalRepository.sendExpenses(
            testExpense(name = "A", price = 20.0, date = twoMonthsAgo),
            testExpense(name = "B", price = 50.0, date = fiveMonthsAgo),
        )

        val expected = (5 downTo 0).map { back ->
            val month = thisMonth.minusMonths(back.toLong())
            val value = when (back) {
                2 -> 20.0
                5 -> 50.0
                else -> 0.0
            }
            BarChartEntry(value, month.year, month.monthValue)
        }
        assertThat(viewModel.barChartData.value).containsExactlyElementsIn(expected).inOrder()
    }

    @Test
    fun barChartData_queriesASixMonthWindowEndingNextMonth() = runTest {
        collect(viewModel.barChartData)
        expensesLocalRepository.sendExpenses(emptyList())

        // The window is [thisMonth - 5, thisMonth + 1); expenses outside it are ignored.
        expensesLocalRepository.sendExpenses(
            testExpense(name = "In", price = 1.0, date = thisMonth.minusMonths(5)),
            testExpense(name = "Out", price = 99.0, date = thisMonth.minusMonths(6)),
        )

        val entries = viewModel.barChartData.value
        assertThat(entries).hasSize(6)
        assertThat(entries.first().value).isEqualTo(1.0)
        assertThat(entries.sumOf { it.value }).isEqualTo(1.0)
    }

    @Test
    fun setBarChartVisibleItems_widensTheWindow() = runTest {
        collect(viewModel.barChartData)
        expensesLocalRepository.sendExpenses(
            testExpense(name = "Old", price = 7.0, date = thisMonth.minusMonths(9)),
        )
        assertThat(viewModel.barChartData.value.sumOf { it.value }).isEqualTo(0.0)

        viewModel.setBarChartVisibleItems(12)

        assertThat(viewModel.barChartData.value).hasSize(12)
        assertThat(viewModel.barChartData.value.sumOf { it.value }).isEqualTo(7.0)
    }

    @Test
    fun barChartNavigation_isClampedForwardButNotBackward() = runTest {
        collect(viewModel.isNextBarChartDateEnabled, viewModel.barChartData)
        expensesLocalRepository.sendExpenses(emptyList())
        assertThat(viewModel.isNextBarChartDateEnabled.value).isFalse()

        viewModel.nextBarChartDate()
        assertThat(viewModel.barChartData.value.last().month).isEqualTo(thisMonth.monthValue)

        viewModel.previousBarChartDate()
        assertThat(viewModel.isNextBarChartDateEnabled.value).isTrue()
        val lastShown = thisMonth.minusMonths(1)
        assertThat(viewModel.barChartData.value.last())
            .isEqualTo(BarChartEntry(0.0, lastShown.year, lastShown.monthValue))

        viewModel.todayBarChartDate()
        assertThat(viewModel.isNextBarChartDateEnabled.value).isFalse()
    }

    @Test
    fun isPreviousBarChartDateEnabled_isTrueOnlyWhileOlderMonthsExist() = runTest {
        collect(viewModel.isPreviousBarChartDateEnabled, viewModel.earliestExpenseDate)
        expensesLocalRepository.sendExpenses(emptyList())
        assertThat(viewModel.isPreviousBarChartDateEnabled.value).isFalse()

        // Earliest expense is exactly the oldest visible month: nothing older to show.
        expensesLocalRepository.sendExpenses(testExpense(date = thisMonth.minusMonths(5)))
        assertThat(viewModel.isPreviousBarChartDateEnabled.value).isFalse()

        expensesLocalRepository.sendExpenses(testExpense(date = thisMonth.minusMonths(6)))
        assertThat(viewModel.isPreviousBarChartDateEnabled.value).isTrue()
    }

    // endregion

    // region pie chart

    @Test
    fun pieChartExpenses_switchBetweenMonthAndYear() = runTest {
        collect(viewModel.pieChartExpenses)
        val thisMonthExpense = testExpense(name = "M", date = today)
        val otherMonth = if (today.monthValue == 1) today.plusMonths(1) else today.minusMonths(1)
        val thisYearExpense = testExpense(name = "Y", date = otherMonth)
        expensesLocalRepository.sendExpenses(thisMonthExpense, thisYearExpense)

        assertThat(viewModel.pieChartExpenses.value).containsExactly(thisMonthExpense)

        viewModel.switchPieChartData(setMonthly = false)
        assertThat(viewModel.pieChartExpenses.value).containsExactly(thisMonthExpense, thisYearExpense)
        assertThat(viewModel.monthlyShownInPieChart.value).isFalse()
    }

    @Test
    fun pieChartDate_monthlyNavigationIsClampedForward() = runTest {
        collect(viewModel.isNextPieChartDateEnabled)
        assertThat(viewModel.pieChartDate.value).isEqualTo(today)
        assertThat(viewModel.isNextPieChartDateEnabled.value).isFalse()

        viewModel.nextPieChartDate()
        assertThat(viewModel.pieChartDate.value).isEqualTo(today)

        viewModel.previousPieChartDate()
        assertThat(viewModel.pieChartDate.value).isEqualTo(today.minusMonths(1))
        assertThat(viewModel.isNextPieChartDateEnabled.value).isTrue()

        viewModel.nextPieChartDate()
        assertThat(viewModel.pieChartDate.value).isEqualTo(today)
    }

    @Test
    fun pieChartDate_annualNavigationIsClampedForward() = runTest {
        collect(viewModel.isNextPieChartDateEnabled)
        viewModel.switchPieChartData(setMonthly = false)

        viewModel.nextPieChartDate()
        assertThat(viewModel.pieChartDate.value).isEqualTo(today)

        viewModel.previousPieChartDate()
        assertThat(viewModel.pieChartDate.value).isEqualTo(today.minusYears(1))
        assertThat(viewModel.isNextPieChartDateEnabled.value).isTrue()
    }

    @Test
    fun pieChartDate_monthlyAndAnnualCursorsAreIndependent() = runTest {
        viewModel.previousPieChartDate() // monthly cursor -> last month
        viewModel.switchPieChartData(setMonthly = false)
        viewModel.previousPieChartDate() // annual cursor -> last year
        assertThat(viewModel.pieChartDate.value).isEqualTo(today.minusYears(1))

        viewModel.switchPieChartData(setMonthly = true)
        assertThat(viewModel.pieChartDate.value).isEqualTo(today.minusMonths(1))

        viewModel.switchPieChartData(setMonthly = false)
        assertThat(viewModel.pieChartDate.value).isEqualTo(today.minusYears(1))
    }

    @Test
    fun todayPieChartDate_resetsTheActiveCursorOnly() = runTest {
        viewModel.previousPieChartDate()
        viewModel.switchPieChartData(setMonthly = false)
        viewModel.previousPieChartDate()

        viewModel.todayPieChartDate()
        assertThat(viewModel.pieChartDate.value).isEqualTo(today)

        viewModel.switchPieChartData(setMonthly = true)
        assertThat(viewModel.pieChartDate.value).isEqualTo(today.minusMonths(1))
    }

    @Test
    fun isPreviousPieChartDateEnabled_dependsOnTheEarliestExpense() = runTest {
        collect(viewModel.isPreviousPieChartDateEnabled, viewModel.earliestExpenseDate)
        expensesLocalRepository.sendExpenses(emptyList())
        assertThat(viewModel.isPreviousPieChartDateEnabled.value).isFalse()

        expensesLocalRepository.sendExpenses(testExpense(date = today.minusMonths(1)))
        assertThat(viewModel.isPreviousPieChartDateEnabled.value).isTrue()

        viewModel.previousPieChartDate()
        assertThat(viewModel.isPreviousPieChartDateEnabled.value).isFalse()

        viewModel.switchPieChartData(setMonthly = false)
        assertThat(viewModel.isPreviousPieChartDateEnabled.value).isEqualTo(today.minusMonths(1).year < today.year)
    }

    // endregion

    @Test
    fun toggleMonthShown_updatesState() {
        viewModel.toggleMonthShown(false)
        assertThat(viewModel.monthShown.value).isFalse()
    }

    @Test
    fun scrollToTop_emitsToSubscribers() = runTest {
        val scrolls = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.scrollToTop.toList(scrolls) }

        viewModel.scrollToTop()

        assertThat(scrolls).hasSize(1)
    }

    private fun TestScope.collect(vararg flows: StateFlow<*>) {
        flows.forEach { flow ->
            backgroundScope.launch(UnconfinedTestDispatcher()) { flow.collect() }
        }
    }
}
