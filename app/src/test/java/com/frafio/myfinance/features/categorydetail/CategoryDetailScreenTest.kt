package com.frafio.myfinance.features.categorydetail

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class CategoryDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val month = LocalDate.of(2024, 5, 1)
    private val dining = ExpenseCategories.get(CATEGORIES.DINING.value)
    private val stats = sampleCategoryDetailStats(month)

    private val events = mutableListOf<String>()

    private fun setScreen(stats: CategoryDetailStats? = this.stats) {
        composeTestRule.setThemedContent {
            CategoryDetailContent(
                category = dining,
                month = month,
                stats = stats,
                onBackClick = { events += "back" },
                onSeeAllExpenses = { events += "seeAll" },
                onFrequentExpenseClick = { events += "frequent:${it.name}" }
            )
        }
    }

    @Test
    fun header_showsTheCategoryMonthAndDescription() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.dining)).assertIsDisplayed()
        composeTestRule.onNodeWithText("May 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.dining_description)).assertIsDisplayed()
    }

    @Test
    fun stats_showTheMonthAverageYearToDateAndShare() {
        setScreen()

        composeTestRule.onNodeWithText(doubleToPrice(stats.monthTotal)).assertIsDisplayed()
        // The trend chart's average covers the same twelve months; the stat card comes first.
        composeTestRule.onAllNodesWithText(doubleToPrice(stats.average)).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(stats.yearToDate)).assertIsDisplayed()
        composeTestRule.onNodeWithText("2024").assertIsDisplayed()
        // 320 of 1140.
        composeTestRule.onNodeWithText("28%").assertIsDisplayed()
    }

    @Test
    fun frequentAndLabelSections_areShown() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.most_frequent)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.by_label)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun seeAll_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithTag("category_see_all").performScrollTo().performClick()

        assertThat(events).containsExactly("seeAll")
    }

    @Test
    fun whileLoading_onlyTheHeaderIsShown() {
        setScreen(stats = null)

        composeTestRule.onNodeWithText(string(R.string.dining_description)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_see_all").assertDoesNotExist()
    }

    @Test
    fun aMonthWithoutSpending_hidesBiggestExpenses_andShowsNoChange() {
        // Spending only three months earlier: nothing this month or the one before.
        val older = testExpense(price = 50.0, date = month.minusMonths(3), category = dining.id)
        setScreen(stats = buildCategoryDetail(month, listOf(older), monthTotalAll = 0.0))

        composeTestRule.onNodeWithText(string(R.string.biggest_expenses)).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(string(R.string.spending_none_description), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun biggestExpenses_areReadOnly() {
        setScreen()
        val biggest = stats.topExpenses.first()

        // The first match is the expense row; the name also appears under Most frequent.
        composeTestRule.onAllNodesWithText(biggest.name)[0]
            .performScrollTo()
            .assert(hasNoClickAction() and !hasAnyAncestor(hasClickAction()))
        composeTestRule.onNodeWithContentDescription(
            string(R.string.change_category, string(R.string.dining)),
            useUnmergedTree = true
        ).assertDoesNotExist()
    }

    @Test
    fun tappingAMostFrequentRow_reportsThatExpense() {
        setScreen()

        // Most used first: Sushi Bar.
        composeTestRule.onAllNodesWithTag("frequent_expense_item")[0].performScrollTo().performClick()

        assertThat(events).containsExactly("frequent:Sushi Bar")
    }

    @Test
    fun labelRows_areNotClickable() {
        setScreen()

        // The last "Friends" is the By label row; the others are label chips on expense rows.
        composeTestRule.onAllNodesWithText("Friends").onLast()
            .performScrollTo()
            .assert(hasNoClickAction() and !hasAnyAncestor(hasClickAction()))
    }

    @Test
    fun trendCard_showsTheRangeButtons_andReportsTheChosenRange() {
        composeTestRule.setThemedContent {
            CategoryDetailContent(
                category = dining,
                month = month,
                stats = stats,
                onBackClick = {},
                onSeeAllExpenses = {},
                onFrequentExpenseClick = {},
                onTrendRangeChange = { events += "range:$it" }
            )
        }

        composeTestRule.onNodeWithText(string(R.string.spending_trend)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.bar_chart_range_dynamic)).performScrollTo().assertIsOn()
        composeTestRule.onNodeWithText(string(R.string.bar_chart_range_1_year)).performScrollTo().assertIsOff().performClick()

        assertThat(events).containsExactly("range:2")
    }

    @Test
    fun trendCard_monthsBeforeTheTrackingStart_haveNoPoint_whileTrackedZeroMonthsDo() {
        composeTestRule.setThemedContent {
            CategoryDetailContent(
                category = dining,
                month = month,
                stats = stats,
                onBackClick = {},
                onSeeAllExpenses = {},
                onFrequentExpenseClick = {},
                trendTrackingStart = LocalDate.of(2024, 4, 1)
            )
        }

        point("March 2024").assertDoesNotExist()
        point("June 2023").assertDoesNotExist()
        // Nothing was spent on dining in April 2024, after tracking started.
        point("April 2024").assertExists()
        point("May 2024").assertExists()
    }

    @Test
    fun trendCard_showsTheAverageOfTheShownMonthsWithData() {
        composeTestRule.setThemedContent {
            CategoryDetailContent(
                category = dining,
                month = month,
                stats = stats,
                onBackClick = {},
                onSeeAllExpenses = {},
                onFrequentExpenseClick = {},
                trendTrackingStart = LocalDate.of(2024, 4, 1)
            )
        }

        // April (nothing spent) and May (320); the months before tracking started are left out.
        composeTestRule.onNodeWithText(averageLabel(2)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(160.0)).assertIsDisplayed()
    }

    private fun averageLabel(months: Int) =
        ApplicationProvider.getApplicationContext<Context>().resources.getQuantityString(R.plurals.trend_average_months, months, months)

    private fun setTrend(shownCount: () -> Int) {
        composeTestRule.setThemedContent {
            CategoryDetailContent(
                category = dining,
                month = month,
                stats = stats,
                onBackClick = {},
                onSeeAllExpenses = {},
                onFrequentExpenseClick = {},
                trendShownCount = shownCount()
            )
        }
    }

    @Test
    fun trendCard_tappingAMonth_showsItInTheHeader_andTappingItAgainShowsTheAverage() {
        setTrend { 12 }

        point("March 2024").performScrollTo().performClick()
        composeTestRule.onNodeWithText("March 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(285.0)).assertIsDisplayed()

        point("March 2024").performClick()
        composeTestRule.onNodeWithText("March 2024").assertDoesNotExist()
        composeTestRule.onNodeWithText(averageLabel(12)).assertIsDisplayed()
    }

    @Test
    fun trendCard_selectedMonth_isClearedOnceTheRangeNoLongerShowsIt() {
        var shownCount by mutableIntStateOf(12)
        setTrend { shownCount }
        point("June 2023").performScrollTo().performClick()
        composeTestRule.onNodeWithText("June 2023").assertIsDisplayed()

        shownCount = 6

        // December 2023 to May 2024: (270 + 250 + 310 + 285 + 0 + 320) / 6.
        composeTestRule.onNodeWithText(doubleToPrice(1435.0 / 6)).assertIsDisplayed()
        composeTestRule.onNodeWithText("June 2023").assertDoesNotExist()
    }

    @Test
    fun trendCard_clearedSelection_staysClearedWhenTheRangeShowsOlderMonths() {
        var shownCount by mutableIntStateOf(6)
        setTrend { shownCount }
        point("March 2024").performScrollTo().performClick()
        point("March 2024").performClick()

        shownCount = 12

        composeTestRule.onNodeWithText(averageLabel(12)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun trendCard_onlyAuto_hidesTheRangeButtons() {
        composeTestRule.setThemedContent {
            CategoryDetailContent(
                category = dining,
                month = month,
                stats = stats,
                onBackClick = {},
                onSeeAllExpenses = {},
                onFrequentExpenseClick = {},
                trendAvailableRanges = listOf(0)
            )
        }

        composeTestRule.onNodeWithText(string(R.string.spending_trend)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.bar_chart_range_dynamic)).assertDoesNotExist()
        composeTestRule.onNodeWithText(string(R.string.bar_chart_range_1_year)).assertDoesNotExist()
    }

    private fun point(monthYear: String) = composeTestRule.onNodeWithContentDescription(monthYear, substring = true)

    @Test
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(events).containsExactly("back")
    }
}
