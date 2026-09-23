package com.frafio.myfinance.features.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class DashboardScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val expensesLocalRepository = TestExpensesLocalRepository()
    private val incomesLocalRepository = TestIncomesLocalRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val loadingRepository = LoadingRepository()
    private lateinit var viewModel: DashboardViewModel

    private val today: LocalDate = LocalDate.now()

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
        userPreferencesRepository.setMonthlyBudget(300.0)
        viewModel = DashboardViewModel(expensesLocalRepository, incomesLocalRepository, userPreferencesRepository, loadingRepository)
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    private fun setScreen() {
        composeTestRule.setThemedContent { DashboardScreen(viewModel = viewModel) }
    }

    private fun populated() {
        loadingRepository.stopFirstSync()
        expensesLocalRepository.sendExpenses(testExpense(name = "Pizza", price = 45.0, date = today), testExpense(name = "Bus", price = 5.0, date = today.minusDays(1)))
        incomesLocalRepository.sendIncomes(testIncome(name = "Salary", price = 2500.0, date = today.withDayOfMonth(1)))
        setScreen()
    }

    @Test
    fun firstSync_showsLoading() {
        setScreen()

        composeTestRule.onNodeWithTag("dashboard_scroll").assertDoesNotExist()
        composeTestRule.onNodeWithText(string(R.string.warning_home)).assertDoesNotExist()
    }

    @Test
    fun noData_showsTheEmptyView() {
        loadingRepository.stopFirstSync()
        expensesLocalRepository.sendExpenses(emptyList())
        incomesLocalRepository.sendIncomes(emptyList())
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.warning_home)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("dashboard_scroll").assertDoesNotExist()
    }

    @Test
    fun withData_showsTheCards() {
        populated()

        composeTestRule.onNodeWithTag("dashboard_scroll").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.expenses_today)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.monthly_expenses)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.expenses_budget)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 300.00").assertIsDisplayed()
    }

    @Test
    fun toggleAnnual_switchesTheBudgetIndicatorToTheYear() {
        populated()

        // Two "Annual" toggles: the budget indicator's comes first, the pie chart's second.
        composeTestRule.onAllNodesWithText(string(R.string.annual))[0].performClick()

        assertThat(viewModel.monthShown.value).isFalse()
        composeTestRule.onNodeWithText("€ 3600.00").assertIsDisplayed()
    }
}
