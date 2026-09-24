package com.frafio.myfinance.app

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.window.core.layout.WindowSizeClass
import com.frafio.myfinance.R
import com.frafio.myfinance.core.components.BarChart
import com.frafio.myfinance.core.components.PieChart
import com.frafio.myfinance.core.components.PieChartItem
import com.frafio.myfinance.core.components.SearchBar
import com.frafio.myfinance.core.components.SheetDialog
import com.frafio.myfinance.core.components.TransactionListItem
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.navigation.HomeTabKey
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.features.add.AddLabelChip
import com.frafio.myfinance.features.add.LabelChip
import com.frafio.myfinance.features.budget.BudgetContent
import com.frafio.myfinance.features.categories.CategoriesScreen
import com.frafio.myfinance.features.dashboard.components.AnnualBalanceCard
import com.frafio.myfinance.features.dashboard.components.ExpensesByCategoryCard
import com.frafio.myfinance.features.dashboard.components.MonthlyExpensesChartCard
import com.frafio.myfinance.features.expenses.components.FilterChipBar
import com.frafio.myfinance.features.home.HomeScreenContent
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.util.performClickAction
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/**
 * What TalkBack hears, for the rules the Roborazzi ATF checks cannot express: that a label names
 * the right thing, that a decorative icon stays silent, and that the charts can be reached at all.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val today = LocalDate.of(2024, 5, 29)

    private val hasAnyContentDescription = SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)

    private fun hasRole(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    private fun hasStateDescription(state: String) = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, state)

    private fun hasClickLabel(label: String) = SemanticsMatcher("click label is $label") {
        it.config.getOrElseNullable(SemanticsActions.OnClick) { null }?.label == label
    }

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    @Test
    fun transactionRow_categoryButton_namesTheCategoryAndTheAction() {
        var clicks = 0
        composeTestRule.setThemedContent {
            TransactionListItem(
                transaction = testExpense(name = "Pizza", category = FirestoreEnums.CATEGORIES.DINING.value),
                indexInGroup = 0, countInGroup = 1, onClick = {}, onLongClick = {}, onIconClick = { clicks++ },
            )
        }

        composeTestRule.onNodeWithContentDescription(string(R.string.change_category, string(R.string.dining)))
            .assert(hasClickAction())
            .performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun filterChip_announcesItsTextOnce_andItsRemoveButtonNamesIt() {
        composeTestRule.setThemedContent {
            FilterChipBar(
                categories = listOf(FirestoreEnums.CATEGORIES.DINING.value), labels = listOf("Dinner"), dateFilter = null,
                getDateLabel = { _, _ -> "" }, onCategoryRemoved = {}, onLabelRemoved = {}, onDateRemoved = {},
            )
        }

        composeTestRule.onAllNodesWithContentDescription("Dinner", useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithContentDescription(string(R.string.dining), useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onNodeWithContentDescription(string(R.string.remove_item, "Dinner")).assert(hasRole(Role.Button))
        composeTestRule.onNodeWithContentDescription(string(R.string.remove_item, string(R.string.dining))).assert(hasRole(Role.Button))
    }

    @Test
    fun barChart_eachBarNamesItsMonthAndAmount() {
        composeTestRule.setThemedContent {
            BarChart(entries = listOf(BarChartEntry(100.0, 2024, 1), BarChartEntry(250.5, 2024, 2)))
        }

        composeTestRule.onNodeWithContentDescription(string(R.string.chart_bar, "January 2024", doubleToPrice(100.0))).assertExists()
        composeTestRule.onNodeWithContentDescription(string(R.string.chart_bar, "February 2024", doubleToPrice(250.5))).assertIsOn()
    }

    @Test
    fun pieChart_arcsAreButtons_andSelectOnClick() {
        composeTestRule.setThemedContent {
            PieChart(
                items = listOf(
                    PieChartItem(value = 30.0, label = "Dining", icon = R.drawable.ic_restaurant_filled),
                    PieChartItem(value = 45.0, label = "Groceries", icon = R.drawable.ic_shopping_cart_filled),
                ),
                animationSpec = snap(),
            )
        }

        val dining = composeTestRule.onNodeWithContentDescription(string(R.string.chart_slice, "Dining", "€ 30"))
        dining.assert(hasRole(Role.Button)).performClickAction()

        dining.assertIsSelected()
        composeTestRule.onNodeWithText("Dining").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(string(R.string.chart_slice, "Groceries", "€ 45")).assertExists()
    }

    @Test
    fun monthlyChartCard_navigationButtonsAreLabelled() {
        composeTestRule.setThemedContent {
            MonthlyExpensesChartCard(barChartData = listOf(BarChartEntry(1.0, 2024, 5)), monthlyBudget = 0.0, onPreviousDate = {}, onNextDate = {}, onToday = {})
        }

        listOf(R.string.previous_month, R.string.next_month, R.string.current_month).forEach {
            composeTestRule.onNodeWithContentDescription(string(it)).assert(hasClickAction())
        }
    }

    @Test
    fun annualBalanceCard_navigationButtonsAreLabelled() {
        composeTestRule.setThemedContent {
            AnnualBalanceCard(balanceYear = 2024, incomesSum = 1.0, expensesSum = 1.0, onPreviousYear = {}, onNextYear = {}, onToday = {})
        }

        listOf(R.string.previous_year, R.string.next_year, R.string.current_year).forEach {
            composeTestRule.onNodeWithContentDescription(string(it)).assert(hasClickAction())
        }
    }

    @Test
    fun expensesByCategoryCard_navigationButtonsAreLabelled() {
        composeTestRule.setThemedContent {
            ExpensesByCategoryCard(
                expenses = listOf(testExpense(date = today)), date = today, monthlyShown = true,
                onSwitchData = {}, onPreviousDate = {}, onNextDate = {}, onToday = {},
            )
        }

        listOf(R.string.previous_period, R.string.next_period, R.string.current_period).forEach {
            composeTestRule.onNodeWithContentDescription(string(it)).assert(hasClickAction())
        }
    }

    @Test
    fun budgetOverview_editAndDeleteAreLabelled() {
        composeTestRule.setThemedContent {
            BudgetContent(
                incomes = emptyList(), isIncomesEmpty = true, monthlyBudget = 1000.0, annualBudget = 12000.0,
                onEditBudgetClick = {}, onDeleteBudget = {}, itemMetadata = emptyMap(), onItemLongClick = { _, _ -> },
                onLoadMore = {}, scrollToIdFlow = flowOf(null),
            )
        }

        composeTestRule.onNodeWithContentDescription(string(R.string.edit_budget)).assert(hasClickAction())
        composeTestRule.onNodeWithContentDescription(string(R.string.delete_budget)).assert(hasClickAction())
    }

    @Test
    fun searchBar_clearIsLabelled_andTheSearchIconIsDecorative() {
        var query = "piz"
        composeTestRule.setThemedContent {
            SearchBar(query = query, onQueryChange = { query = it }, onFilterClick = {})
        }

        composeTestRule.onAllNodesWithContentDescription(string(R.string.search), useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onNodeWithContentDescription(string(R.string.clear_search)).performClick()

        assertThat(query).isEmpty()
    }

    @Test
    fun categoryRow_reportsWhetherItIsExpanded() {
        composeTestRule.setThemedContent { CategoriesScreen(onBackClick = {}) }
        val housing = composeTestRule.onNodeWithText(string(R.string.housing))

        housing.assert(hasStateDescription(string(R.string.collapsed))).performClick()

        housing.assert(hasStateDescription(string(R.string.expanded)))
    }

    @Test
    fun labelChips_removeByClickLabel_andAddIsLabelled() {
        composeTestRule.setThemedContent {
            Box {
                LabelChip(label = "Travel", onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Travel")
            .assert(hasClickLabel(string(R.string.remove)))
            .assert(hasRole(Role.Button))
            .assert(!hasAnyContentDescription)
    }

    @Test
    fun addLabelChip_isLabelled() {
        composeTestRule.setThemedContent { AddLabelChip(onClick = {}) }

        composeTestRule.onNodeWithContentDescription(string(R.string.add_label)).assert(hasRole(Role.Button))
    }

    @Test
    fun homeTopBarAction_isLogoutOnProfile_andProfileElsewhere() {
        var tab by mutableStateOf<HomeTabKey>(HomeTabKey.Dashboard)
        composeTestRule.setThemedContent {
            HomeScreenContent(
                appState = rememberMyFinanceAppState(),
                currentTab = tab,
                profilePicture = null,
                proPicChoice = null,
                windowAdaptiveInfo = WindowAdaptiveInfo(WindowSizeClass(411, 891), Posture()),
                onTabClick = {}, onAddClick = {}, onLogoutClick = {}, onProPicClick = {},
            ) {
                Box(Modifier.fillMaxSize()) { Text("Screen content") }
            }
        }

        composeTestRule.onNodeWithTag("home_top_bar_action").assert(hasContentDescription(string(R.string.profile)))
        tab = HomeTabKey.Profile
        composeTestRule.onNodeWithTag("home_top_bar_action").assert(hasContentDescription(string(R.string.logout)))
    }

    @Test
    fun sheetDialog_headerIconIsDecorative() {
        composeTestRule.setThemedContent {
            SheetDialog(icon = R.drawable.ic_person_filled, title = "Title", label = "Label") {}
        }

        composeTestRule.onAllNodes(hasAnyContentDescription, useUnmergedTree = true).assertCountEquals(0)
    }
}
