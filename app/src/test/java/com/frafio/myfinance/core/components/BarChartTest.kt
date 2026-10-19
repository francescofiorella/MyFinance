package com.frafio.myfinance.core.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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

    // January 2023 to December 2024, oldest first.
    private val twoYears = (0 until 24).map { BarChartEntry(value = 10.0 + it, year = 2023 + it / 12, month = it % 12 + 1) }

    @Test
    fun manyEntries_areAllShown_withTheAxisThinnedOut() {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = twoYears) }
        }

        // 300 dp / 24 = 12.5 dp per bar → a label every 2 bars, counting back from December 2024.
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(24)
        composeTestRule.onNodeWithText("12/24").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("11").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("10").assertCountEquals(2)
        // Only December 2023: December 2024 is selected and shows month and year.
        composeTestRule.onAllNodesWithText("12").assertCountEquals(1)
    }

    @Test
    fun withoutEntryAnimation_entryChangesApplyAtOnce() {
        var shown by mutableStateOf(twoYears.takeLast(12))
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) { BarChart(entries = shown) }
        }
        composeTestRule.mainClock.autoAdvance = false

        // Applied at once, as the paused clock doesn't send snapshot notifications.
        Snapshot.withMutableSnapshot { shown = twoYears.takeLast(6) }
        // Well within an animation, but enough for the ButtonGroup to rebuild its items.
        composeTestRule.mainClock.advanceTimeBy(100)

        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(6)
    }

    @Test
    fun withEntryAnimation_removedBarsShrinkOutBeforeDisappearing() {
        var shown by mutableStateOf(twoYears.takeLast(12))
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) {
                BarChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500))
            }
        }
        composeTestRule.mainClock.autoAdvance = false

        // Applied at once, as the paused clock doesn't send snapshot notifications.
        Snapshot.withMutableSnapshot { shown = twoYears.takeLast(6) }
        composeTestRule.mainClock.advanceTimeBy(100)
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(12)

        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(6)
        composeTestRule.onNodeWithText("07").assertIsDisplayed()
        composeTestRule.onNodeWithText("06").assertDoesNotExist()
    }

    @Test
    fun withEntryAnimation_growingToManyBars_measuresWithoutOverflowing() {
        var shown by mutableStateOf(twoYears.takeLast(6))
        // The dashboard card's chart width on a 411 dp phone, where new bars used to overflow.
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(331.dp)) {
                BarChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500))
            }
        }

        shown = twoYears.takeLast(12)
        composeTestRule.waitForIdle()
        shown = twoYears
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(24)
    }

    @Test
    fun withEntryAnimation_addedBarsAreShown_andTheLatestStaysSelected() {
        var shown by mutableStateOf(twoYears.takeLast(6))
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) {
                BarChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500))
            }
        }

        shown = twoYears.takeLast(12)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(12)
        composeTestRule.onNodeWithText("01").assertIsDisplayed()
        composeTestRule.onNodeWithText("12/24").assertIsDisplayed()
    }
}
