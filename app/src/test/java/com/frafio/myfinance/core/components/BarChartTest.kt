package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.util.setThemedContent
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BarChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Four bars at 40 + 2 * 3 dp each fit the 300 dp box used below without overflowing the ButtonGroup.
    private val entries = listOf(
        BarChartEntry(value = 90.0, year = 2024, month = 9),
        BarChartEntry(value = 100.0, year = 2024, month = 10),
        BarChartEntry(value = 110.0, year = 2024, month = 11),
        BarChartEntry(value = 120.0, year = 2024, month = 12),
    )

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    @Test
    fun header_showsTheLastEntryByDefault() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = entries) }
        }

        composeTestRule.onNodeWithText("€ 120.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("December 2024").assertIsDisplayed()
    }

    @Test
    fun axis_showsTwoDigitMonths_andTheSelectedOneAsMonthYear() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = entries) }
        }

        composeTestRule.onNodeWithText("09").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("11").assertIsDisplayed()
        composeTestRule.onNodeWithText("12/24").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertDoesNotExist()
    }

    @Test
    fun barClick_selectsItAndReportsTheIndex() {
        val clicked = mutableListOf<Int>()
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = entries, onBarClick = { clicked += it }) }
        }

        composeTestRule.onAllNodes(isToggleable())[1].performClick()

        assertThat(clicked).containsExactly(1)
        composeTestRule.onNodeWithText("€ 100.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("October 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("10/24").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun clickingTheSelectedBar_doesNotReportAgain() {
        val clicked = mutableListOf<Int>()
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = entries, onBarClick = { clicked += it }) }
        }

        composeTestRule.onAllNodes(isToggleable())[3].performClick()

        assertThat(clicked).isEmpty()
    }

    // The scale comes from the tallest bar, so the reference line only exists at or below it; the
    // label is added when the last bar is within the reference.
    private val unevenEntries = listOf(
        BarChartEntry(value = 90.0, year = 2024, month = 9),
        BarChartEntry(value = 150.0, year = 2024, month = 10),
        BarChartEntry(value = 110.0, year = 2024, month = 11),
        BarChartEntry(value = 100.0, year = 2024, month = 12),
    )

    @Test
    fun referenceValue_showsTheBudgetLabel_whenTheLastEntryIsWithinIt() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = unevenEntries, referenceValue = 120.0) }
        }

        composeTestRule.onNodeWithText("€ 120").assertIsDisplayed()
    }

    @Test
    fun referenceValue_hidesTheBudgetLabel_whenTheLastEntryExceedsIt() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = unevenEntries, referenceValue = 95.0) }
        }

        composeTestRule.onNodeWithText("€ 95").assertDoesNotExist()
    }

    @Test
    fun referenceValue_aboveTheTallestBar_isNotDrawn() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = unevenEntries, referenceValue = 200.0) }
        }

        composeTestRule.onNodeWithText("€ 200").assertDoesNotExist()
    }

    @Test
    fun noReferenceValue_showsNoBudgetLabel() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = unevenEntries) }
        }

        composeTestRule.onNodeWithText("€ 120").assertDoesNotExist()
    }

    @Test
    fun emptyEntries_rendersWithoutBars() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = emptyList()) }
        }

        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(0)
        composeTestRule.onNodeWithText("December 2024").assertDoesNotExist()
    }

    @Test
    fun visibleCount_isReportedFromTheAvailableWidth() {
        val reported = mutableListOf<Int>()
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = entries, onVisibleCountChanged = { reported += it }) }
        }

        // 300 dp / (40 + 2 * 3) dp = 6.5 → 6
        assertThat(reported).containsExactly(6)
    }

    @Test
    fun visibleCount_honoursCustomBarSizes() {
        val reported = mutableListOf<Int>()
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) {
                BarChart(entries = entries, barWidth = 20.dp, barPadding = 5.dp, onVisibleCountChanged = { reported += it })
            }
        }

        // 300 dp / (20 + 2 * 5) dp = 10
        assertThat(reported).containsExactly(10)
    }
}
