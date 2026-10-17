package com.frafio.myfinance.features.categorydetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.utils.doubleToPrice
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
                onSeeAllExpenses = { events += "seeAll" }
            )
        }
    }

    @Test
    fun header_showsTheCategoryMonthAndDescription() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.dining)).assertIsDisplayed()
        // The trend chart also labels its selected bar "May 2024"; the header comes first.
        composeTestRule.onAllNodesWithText("May 2024").onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.dining_description)).assertIsDisplayed()
    }

    @Test
    fun stats_showTheMonthAverageYearToDateAndShare() {
        setScreen()

        // The trend chart repeats the selected month's total; the stat card comes first.
        composeTestRule.onAllNodesWithText(doubleToPrice(stats.monthTotal)).onFirst().assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(stats.average)).assertIsDisplayed()
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
    fun back_invokesTheCallback() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(events).containsExactly("back")
    }
}
