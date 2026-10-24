package com.frafio.charts

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class BarChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Four bars at 40 + 2 * 3 dp each fit the 300 dp box used below without overflowing the ButtonGroup.
    private val entries = listOf(
        entry(value = 90.0, year = 2024, month = 9),
        entry(value = 100.0, year = 2024, month = 10),
        entry(value = 110.0, year = 2024, month = 11),
        entry(value = 120.0, year = 2024, month = 12),
    )

    /** A monthly bar, keyed and labelled the way the app builds them. */
    private fun entry(value: Double, year: Int, month: Int) = BarChartEntry(
        key = year * 12 + month - 1,
        value = value,
        label = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)),
        axisLabel = "%02d".format(month),
        selectedAxisLabel = "%02d/%02d".format(month, year % 100)
    )

    private fun setChart(content: @Composable () -> Unit) = composeTestRule.setContent { MaterialTheme(content = content) }

    /** The chart with euro amounts: two decimals in the header, none on the reference line. */
    @Composable
    private fun TestBarChart(
        entries: List<BarChartEntry>,
        referenceValue: Double? = null,
        onBarClick: (Int) -> Unit = {},
        onVisibleCountChanged: (Int) -> Unit = {},
        barWidth: Dp = BarChartDefaults.BarWidth,
        barPadding: Dp = BarChartDefaults.BarPadding,
        entryAnimationSpec: FiniteAnimationSpec<Float>? = null
    ) = BarChart(
        entries = entries,
        referenceValue = referenceValue,
        onBarClick = onBarClick,
        onVisibleCountChanged = onVisibleCountChanged,
        valueFormatter = { "€ %.2f".format(Locale.US, it) },
        referenceValueFormatter = { "€ %.0f".format(Locale.US, it) },
        barWidth = barWidth,
        barPadding = barPadding,
        entryAnimationSpec = entryAnimationSpec
    )

    @Test
    fun header_showsTheLastEntryByDefault() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = entries) }
        }

        composeTestRule.onNodeWithText("€ 120.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("December 2024").assertIsDisplayed()
    }

    @Test
    fun axis_showsTwoDigitMonths_andTheSelectedOneAsMonthYear() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = entries) }
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
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = entries, onBarClick = { clicked += it }) }
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
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = entries, onBarClick = { clicked += it }) }
        }

        composeTestRule.onAllNodes(isToggleable())[3].performClick()

        assertThat(clicked).isEmpty()
    }

    // The scale comes from the tallest bar, so the reference line only exists at or below it; the
    // label is added when the last bar is within the reference.
    private val unevenEntries = listOf(
        entry(value = 90.0, year = 2024, month = 9),
        entry(value = 150.0, year = 2024, month = 10),
        entry(value = 110.0, year = 2024, month = 11),
        entry(value = 100.0, year = 2024, month = 12),
    )

    @Test
    fun referenceValue_showsTheBudgetLabel_whenTheLastEntryIsWithinIt() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = unevenEntries, referenceValue = 120.0) }
        }

        composeTestRule.onNodeWithText("€ 120").assertIsDisplayed()
    }

    @Test
    fun referenceValue_hidesTheBudgetLabel_whenTheLastEntryExceedsIt() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = unevenEntries, referenceValue = 95.0) }
        }

        composeTestRule.onNodeWithText("€ 95").assertDoesNotExist()
    }

    @Test
    fun referenceValue_aboveTheTallestBar_isNotDrawn() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = unevenEntries, referenceValue = 200.0) }
        }

        composeTestRule.onNodeWithText("€ 200").assertDoesNotExist()
    }

    @Test
    fun noReferenceValue_showsNoBudgetLabel() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = unevenEntries) }
        }

        composeTestRule.onNodeWithText("€ 120").assertDoesNotExist()
    }

    @Test
    fun emptyEntries_rendersWithoutBars() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = emptyList()) }
        }

        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(0)
        composeTestRule.onNodeWithText("December 2024").assertDoesNotExist()
    }

    @Test
    fun visibleCount_isReportedFromTheAvailableWidth() {
        val reported = mutableListOf<Int>()
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = entries, onVisibleCountChanged = { reported += it }) }
        }

        // 300 dp / (40 + 2 * 3) dp = 6.5 → 6
        assertThat(reported).containsExactly(6)
    }

    @Test
    fun visibleCount_honoursCustomBarSizes() {
        val reported = mutableListOf<Int>()
        setChart {
            Box(Modifier.requiredWidth(300.dp)) {
                TestBarChart(entries = entries, barWidth = 20.dp, barPadding = 5.dp, onVisibleCountChanged = { reported += it })
            }
        }

        // 300 dp / (20 + 2 * 5) dp = 10
        assertThat(reported).containsExactly(10)
    }

    private fun bar(monthYear: String) = composeTestRule.onNodeWithContentDescription(monthYear, substring = true)

    // January 2023 to December 2024, oldest first.
    private val twoYears = (0 until 24).map { entry(value = 10.0 + it, year = 2023 + it / 12, month = it % 12 + 1) }

    @Test
    fun manyEntries_areAllShown_withTheAxisThinnedOut() {
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = twoYears) }
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
        setChart {
            Box(Modifier.requiredWidth(300.dp)) { TestBarChart(entries = shown) }
        }
        composeTestRule.mainClock.autoAdvance = false

        // Applied at once, as the paused clock doesn't send snapshot notifications.
        Snapshot.withMutableSnapshot { shown = twoYears.takeLast(6) }
        // Well within an animation, but enough for the ButtonGroup to rebuild its items.
        composeTestRule.mainClock.advanceTimeBy(100)

        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(6)
    }

    @Test
    fun withEntryAnimation_removedBarsSlideOutAtTheSameWidthAsTheOthers() {
        var shown by mutableStateOf(twoYears.takeLast(12))
        setChart {
            Box(Modifier.requiredWidth(300.dp)) {
                TestBarChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500))
            }
        }
        composeTestRule.mainClock.autoAdvance = false

        // Applied at once, as the paused clock doesn't send snapshot notifications.
        Snapshot.withMutableSnapshot { shown = twoYears.takeLast(6) }
        composeTestRule.mainClock.advanceTimeBy(100)
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(12)
        val leaving = bar("January 2024").getUnclippedBoundsInRoot().width
        val staying = bar("December 2024").getUnclippedBoundsInRoot().width
        assertThat(leaving.value).isWithin(1f).of(staying.value)

        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(6)
        composeTestRule.onNodeWithText("07").assertIsDisplayed()
        composeTestRule.onNodeWithText("06").assertDoesNotExist()
    }

    @Test
    fun withEntryAnimation_remainingBarsKeepTheirHeight_whenRemovedBarsAreDropped() {
        var shown by mutableStateOf(twoYears.takeLast(12))
        setChart {
            Box(Modifier.requiredWidth(300.dp)) {
                TestBarChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500))
            }
        }
        composeTestRule.mainClock.autoAdvance = false

        Snapshot.withMutableSnapshot { shown = twoYears.takeLast(6) }
        var frames = 0
        while (composeTestRule.onAllNodes(isToggleable()).fetchSemanticsNodes().size != 6 && frames++ < 200) {
            composeTestRule.mainClock.advanceTimeByFrame()
        }

        // On the first frame without the removed bars, December 2024 (the tallest) is still full height.
        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(6)
        val height = bar("December 2024").getUnclippedBoundsInRoot().height
        assertThat(height.value).isWithin(1f).of(BarChartDefaults.BarMaxHeight.value)
    }

    @Test
    fun withEntryAnimation_growingToManyBars_measuresWithoutOverflowing() {
        var shown by mutableStateOf(twoYears.takeLast(6))
        // The dashboard card's chart width on a 411 dp phone, where new bars used to overflow.
        setChart {
            Box(Modifier.requiredWidth(331.dp)) {
                TestBarChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500))
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
        setChart {
            Box(Modifier.requiredWidth(300.dp)) {
                TestBarChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500))
            }
        }

        shown = twoYears.takeLast(12)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isToggleable()).assertCountEquals(12)
        composeTestRule.onNodeWithText("01").assertIsDisplayed()
        composeTestRule.onNodeWithText("12/24").assertIsDisplayed()
    }
}
