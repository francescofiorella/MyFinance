package com.frafio.myfinance.features.dashboard.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.data.testExpense
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

/** The five dashboard cards; the bar and pie charts inside them are covered in `core/components`. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class DashboardCardsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()
    private val today = LocalDate.of(2024, 5, 29)

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    /**
     * The prev / next / today buttons come first in tree order; the other plain clickables are the
     * zero-size overflow indicators of the `ButtonGroup`s, and the chart bars are toggleable.
     */
    private fun navButtons() = composeTestRule.onAllNodes(hasClickAction() and !isToggleable())

    // region BudgetIndicatorCard

    @Test
    fun budgetIndicator_withBudget_showsAmountBudgetAndToggles() {
        composeTestRule.setThemedContent {
            BudgetIndicatorCard(monthShown = true, thisMonthSum = 450.0, thisYearSum = 5400.0, monthlyBudget = 1000.0, onToggleMonthShown = { events += "toggle:$it" })
        }

        composeTestRule.onNodeWithText("€ 450.00").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.expenses_budget)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 1000.00").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.annual)).performClick()
        composeTestRule.onNodeWithText(string(R.string.monthly)).performClick()

        assertThat(events).containsExactly("toggle:false", "toggle:true").inOrder()
    }

    @Test
    fun budgetIndicator_annual_showsYearSumAgainstTwelveBudgets() {
        composeTestRule.setThemedContent {
            BudgetIndicatorCard(monthShown = false, thisMonthSum = 450.0, thisYearSum = 5400.0, monthlyBudget = 1000.0, onToggleMonthShown = {})
        }

        composeTestRule.onNodeWithText("€ 5400.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 12000.00").assertIsDisplayed()
    }

    @Test
    fun budgetIndicator_withoutBudget_showsOnlyTheMonthSum() {
        composeTestRule.setThemedContent {
            BudgetIndicatorCard(monthShown = true, thisMonthSum = 450.0, thisYearSum = 5400.0, monthlyBudget = 0.0, onToggleMonthShown = {})
        }

        composeTestRule.onNodeWithText("€ 450.00").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.this_month)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.expenses_budget)).assertDoesNotExist()
        composeTestRule.onNodeWithText(string(R.string.annual)).assertDoesNotExist()
    }

    @Test
    fun budgetIndicator_overBudget_stillShowsTheAmount() {
        composeTestRule.setThemedContent {
            BudgetIndicatorCard(monthShown = true, thisMonthSum = 1500.0, thisYearSum = 5400.0, monthlyBudget = 1000.0, onToggleMonthShown = {})
        }

        composeTestRule.onNodeWithText("€ 1500.00").assertIsDisplayed()
    }

    // endregion

    // region MonthlyExpensesChartCard

    private val entries = (9..12).map { BarChartEntry(value = it * 10.0, year = 2024, month = it) }

    @Test
    fun monthlyChart_navigationButtons_honourFlagsAndInvokeCallbacks() {
        composeTestRule.setThemedContent {
            MonthlyExpensesChartCard(
                barChartData = entries, monthlyBudget = 0.0,
                onPreviousDate = { events += "prev" }, onNextDate = { events += "next" }, onToday = { events += "today" },
                isNextDateEnabled = false,
            )
        }

        navButtons()[0].assertIsDisplayed().assertIsEnabled().performClick()
        navButtons()[1].assertIsNotEnabled()
        navButtons()[2].performClick()

        assertThat(events).containsExactly("prev", "today").inOrder()
        composeTestRule.onNodeWithText(string(R.string.monthly_expenses)).assertIsDisplayed()
    }

    @Test
    fun monthlyChart_previousDisabled() {
        composeTestRule.setThemedContent {
            MonthlyExpensesChartCard(barChartData = entries, monthlyBudget = 0.0, onPreviousDate = {}, onNextDate = { events += "next" }, onToday = {}, isPreviousDateEnabled = false)
        }

        navButtons()[0].assertIsNotEnabled()
        navButtons()[1].assertIsEnabled().performClick()

        assertThat(events).containsExactly("next")
    }

    // endregion

    // region AnnualBalanceCard

    @Test
    fun annualBalance_showsYearAndBalance_withDecimalsBelowAThousand() {
        composeTestRule.setThemedContent {
            AnnualBalanceCard(balanceYear = 2024, incomesSum = 900.0, expensesSum = 650.5, onPreviousYear = {}, onNextYear = {}, onToday = {})
        }

        composeTestRule.onNodeWithText("2024").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.annual_balance)).assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 249.50").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 900.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 650.50").assertIsDisplayed()
    }

    @Test
    fun annualBalance_largeSums_dropDecimals_andNegativeBalanceIsShownAbsolute() {
        composeTestRule.setThemedContent {
            AnnualBalanceCard(balanceYear = 2024, incomesSum = 12000.0, expensesSum = 13500.0, onPreviousYear = {}, onNextYear = {}, onToday = {})
        }

        composeTestRule.onNodeWithText("€ 12000").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 13500").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 1500.00").assertIsDisplayed()
    }

    @Test
    fun annualBalance_navigationButtons_honourFlagsAndInvokeCallbacks() {
        composeTestRule.setThemedContent {
            AnnualBalanceCard(
                balanceYear = 2024, incomesSum = 1.0, expensesSum = 1.0,
                onPreviousYear = { events += "prev" }, onNextYear = { events += "next" }, onToday = { events += "today" },
                isPreviousYearEnabled = false, isNextYearEnabled = true,
            )
        }

        navButtons()[0].assertIsNotEnabled()
        navButtons()[1].assertIsEnabled().performClick()
        navButtons()[2].performClick()

        assertThat(events).containsExactly("next", "today").inOrder()
    }

    @Test
    fun annualBalance_nextYearDisabled() {
        composeTestRule.setThemedContent {
            AnnualBalanceCard(balanceYear = 2024, incomesSum = 1.0, expensesSum = 1.0, onPreviousYear = {}, onNextYear = {}, onToday = {}, isNextYearEnabled = false)
        }

        navButtons()[1].assertIsNotEnabled()
    }

    // endregion

    // region ExpensesCard

    @Test
    fun expensesCard_monthShown_showsTodayAndTheYear() {
        composeTestRule.setThemedContent {
            ExpensesCard(todaySum = 45.0, monthShown = true, thisMonthSum = 450.0, thisYearSum = 3350.0, today = today)
        }

        composeTestRule.onNodeWithText(string(R.string.expenses_today)).assertIsDisplayed()
        composeTestRule.onNodeWithText("29 May 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 45.00").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.this_year_next)).assertIsDisplayed()
        composeTestRule.onNodeWithText("2024").assertIsDisplayed()
        // The amount's own size picks the format, not today's.
        composeTestRule.onNodeWithText("€ 3350").assertIsDisplayed()
    }

    @Test
    fun expensesCard_annualShown_showsTheMonth() {
        composeTestRule.setThemedContent {
            ExpensesCard(todaySum = 2000.0, monthShown = false, thisMonthSum = 450.0, thisYearSum = 3350.0, today = today)
        }

        composeTestRule.onNodeWithText("€ 2000").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.this_month)).assertIsDisplayed()
        composeTestRule.onNodeWithText("May 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("€ 450.00").assertIsDisplayed()
    }

    // endregion

    // region ExpensesByCategoryCard

    @Test
    fun byCategory_monthly_showsMonthAndYear_andSwitchesData() {
        composeTestRule.setThemedContent {
            ExpensesByCategoryCard(
                expenses = listOf(testExpense(date = today)), date = today, monthlyShown = true,
                onSwitchData = { events += "switch:$it" }, onPreviousDate = {}, onNextDate = {}, onToday = {},
            )
        }

        composeTestRule.onNodeWithText(string(R.string.expenses_by_category)).assertIsDisplayed()
        composeTestRule.onNodeWithText("May 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.annual)).performClick()
        composeTestRule.onNodeWithText(string(R.string.monthly)).performClick()

        assertThat(events).containsExactly("switch:false", "switch:true").inOrder()
    }

    @Test
    fun byCategory_annual_showsOnlyTheYear_andNavigates() {
        composeTestRule.setThemedContent {
            ExpensesByCategoryCard(
                expenses = emptyList(), date = today, monthlyShown = false,
                onSwitchData = {}, onPreviousDate = { events += "prev" }, onNextDate = { events += "next" }, onToday = { events += "today" },
                isNextDateEnabled = false,
            )
        }

        composeTestRule.onNodeWithText("2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("May 2024").assertDoesNotExist()
        navButtons()[0].performClick()
        navButtons()[1].assertIsNotEnabled()
        navButtons()[2].performClick()

        assertThat(events).containsExactly("prev", "today").inOrder()
    }

    // endregion
}
