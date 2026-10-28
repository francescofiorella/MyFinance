package com.frafio.myfinance.testing.screenshot

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.features.budget.BudgetContent
import com.frafio.myfinance.features.dashboard.DashboardContent
import com.frafio.myfinance.features.expenses.ExpensesContent
import com.frafio.myfinance.features.profile.ProfileContent
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/** The tab screens filled with the screenshot fixtures, shared by the screen goldens and the README ones. */

@Composable
internal fun ScreenshotDashboard(
    monthlyBudget: Double,
    shownCount: Int = screenshotBarChart.size,
    availableRanges: List<Int> = ALL_MONTH_RANGES,
    trackingStart: LocalDate? = null
) {
    DashboardContent(
        today = screenshotToday,
        monthShown = true,
        thisMonthSum = 450.0,
        thisYearSum = 3350.0,
        monthlyBudget = monthlyBudget,
        todaySum = 45.0,
        balanceYear = 2024,
        incomesSum = 12000.0,
        expensesSum = 3350.0,
        barChartData = screenshotBarChart,
        pieExpenses = screenshotExpenses,
        pieDate = screenshotToday,
        monthlyShownInPie = true,
        scrollState = rememberScrollState(),
        onToggleMonthShown = {},
        onPreviousYear = {},
        onNextYear = {},
        onTodayAnnualBalance = {},
        onPreviousBarDate = {},
        onNextBarDate = {},
        onTodayBarDate = {},
        barChartShownCount = shownCount,
        barChartAvailableRanges = availableRanges,
        barChartTrackingStart = trackingStart,
        onSwitchPieData = {},
        onPreviousPieDate = {},
        onNextPieDate = {},
        onTodayPieData = {},
    )
}

@Composable
internal fun ScreenshotExpenses(
    expenses: List<Expense>,
    isEmpty: Boolean?,
    searchQuery: String = "",
    isSearchExpanded: Boolean = false,
    selectedCategories: List<Int> = emptyList(),
    selectedLabels: List<String> = emptyList(),
    dateRange: Pair<LocalDate, LocalDate>? = null,
) {
    ExpensesContent(
        expenses = expenses,
        totalFilteredExpenses = 46.5,
        isExpensesEmpty = isEmpty,
        searchQuery = searchQuery,
        isSearchExpanded = isSearchExpanded,
        selectedCategories = selectedCategories,
        selectedLabels = selectedLabels,
        dateRange = dateRange,
        scrollToIdFlow = flowOf(null),
        itemMetadata = screenshotExpensesMetadata,
        onSearchQueryChanged = {},
        onSearchExpandedChange = {},
        onCategoryFilterChanged = {},
        onLabelFilterChanged = { _, _ -> },
        onDateFilterChanged = {},
        onLoadMore = {},
        onFilterClick = {},
        onItemLongClick = { _, _ -> },
        onCategoryClick = { _, _ -> },
        onCategoriesClick = {},
        onManageLabels = {},
        getDateLabel = ::screenshotDateLabel,
    )
}

@Composable
internal fun ScreenshotBudget(incomes: List<Income>, isEmpty: Boolean?, monthlyBudget: Double) {
    BudgetContent(
        incomes = incomes,
        isIncomesEmpty = isEmpty,
        monthlyBudget = monthlyBudget,
        annualBudget = monthlyBudget * 12,
        onEditBudgetClick = {},
        onDeleteBudget = {},
        itemMetadata = screenshotIncomesMetadata,
        onItemLongClick = { _, _ -> },
        onLoadMore = {},
        scrollToIdFlow = flowOf(null),
    )
}

@Composable
internal fun ScreenshotProfile(user: User) {
    ProfileContent(
        user = user,
        profilePicture = null,
        proPicChoice = null,
        versionName = "MyFinance 5.0.3",
        isDynamicColorAvailable = true,
        isDynamicColorChecked = false,
        scrollState = rememberScrollState(),
        onSelectProPic = {},
        onEditFullName = {},
        onDynamicColorChanged = {},
        onManageLabels = {},
        onManageCategories = {},
        onSelectCurrency = {},
        onChangePasswordClick = {},
        currencyCode = "EUR",
    )
}
