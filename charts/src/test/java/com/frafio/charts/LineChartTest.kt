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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class LineChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val entries = listOf(
        entry(value = 90.0, year = 2024, month = 9),
        entry(value = 100.0, year = 2024, month = 10),
        entry(value = 110.0, year = 2024, month = 11),
        entry(value = 120.0, year = 2024, month = 12),
    )

    /** A monthly point, keyed and labelled like the bar chart's; a null [value] is a month without data. */
    private fun entry(value: Double?, year: Int, month: Int) = LineChartEntry(
        key = year * 12 + month - 1,
        value = value,
        label = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)),
        axisLabel = "%02d".format(month)
    )

    private fun setChart(content: @Composable () -> Unit) = composeTestRule.setContent { MaterialTheme(content = content) }

    @Composable
    private fun TestLineChart(
        entries: List<LineChartEntry>,
        onVisibleCountChanged: (Int) -> Unit = {},
        pointSpacing: Dp = LineChartDefaults.PointSpacing,
        entryAnimationSpec: FiniteAnimationSpec<Float>? = null
    ) = Box(Modifier.requiredWidth(300.dp)) {
        LineChart(
            entries = entries,
            onVisibleCountChanged = onVisibleCountChanged,
            valueFormatter = { "€ %.2f".format(Locale.US, it) },
            pointSpacing = pointSpacing,
            entryAnimationSpec = entryAnimationSpec
        )
    }

    private val points = SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)

    private fun point(monthYear: String) = composeTestRule.onNodeWithContentDescription(monthYear, substring = true)

    private fun DpRect.centerX() = (left + right).value / 2

    private fun DpRect.centerY() = (top + bottom).value / 2

    @Test
    fun axis_showsEveryEntryLabel() {
        setChart { TestLineChart(entries = entries) }

        composeTestRule.onNodeWithText("09").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("11").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun everyEntryWithData_hasAPointDescribingIt() {
        setChart { TestLineChart(entries = entries) }

        composeTestRule.onAllNodes(points).assertCountEquals(4)
        composeTestRule.onNodeWithContentDescription("October 2024, € 100.00").assertIsDisplayed()
    }

    // September and October have no data; November is a real zero.
    private val partlyTracked = listOf(
        entry(value = null, year = 2024, month = 9),
        entry(value = null, year = 2024, month = 10),
        entry(value = 0.0, year = 2024, month = 11),
        entry(value = 120.0, year = 2024, month = 12),
    )

    @Test
    fun entriesWithoutData_drawNoPoint_butKeepTheirAxisLabel() {
        setChart { TestLineChart(entries = partlyTracked) }

        composeTestRule.onAllNodes(points).assertCountEquals(2)
        point("September 2024").assertDoesNotExist()
        point("October 2024").assertDoesNotExist()
        point("November 2024").assertExists()
        composeTestRule.onNodeWithText("09").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun points_spanThePlotHeight_fromZeroToTheLargestValue() {
        setChart { TestLineChart(entries = partlyTracked) }

        val zero = point("November 2024").getUnclippedBoundsInRoot()
        val largest = point("December 2024").getUnclippedBoundsInRoot()
        assertThat((zero.bottom - largest.top).value).isWithin(1f).of(LineChartDefaults.PlotHeight.value)
        assertThat(largest.height.value).isWithin(0.5f).of(LineChartDefaults.PointSize.value)
    }

    @Test
    fun points_areScaledOnTheLargestValue() {
        val halves = listOf(
            entry(value = 0.0, year = 2024, month = 10),
            entry(value = 60.0, year = 2024, month = 11),
            entry(value = 120.0, year = 2024, month = 12),
        )
        setChart { TestLineChart(entries = halves) }

        val zero = point("October 2024").getUnclippedBoundsInRoot()
        val half = point("November 2024").getUnclippedBoundsInRoot()
        val largest = point("December 2024").getUnclippedBoundsInRoot()
        assertThat(half.centerY()).isWithin(1f).of((zero.centerY() + largest.centerY()) / 2)
    }

    @Test
    fun points_areCentredOnTheirAxisLabels_withSlotsSplittingTheWidth() {
        setChart { TestLineChart(entries = partlyTracked) }

        val december = point("December 2024").getUnclippedBoundsInRoot()
        val decemberLabel = composeTestRule.onNodeWithText("12").getUnclippedBoundsInRoot()
        val septemberLabel = composeTestRule.onNodeWithText("09").getUnclippedBoundsInRoot()
        assertThat(december.centerX()).isWithin(1f).of(decemberLabel.centerX())
        // Four slots share the width, entries without data included.
        assertThat(december.centerX()).isWithin(1f).of(300f * 7 / 8)
        assertThat(decemberLabel.centerX() - septemberLabel.centerX()).isWithin(1f).of(3 * 300f / 4)
    }

    @Test
    fun emptyEntries_rendersWithoutPoints() {
        setChart { TestLineChart(entries = emptyList()) }

        composeTestRule.onAllNodes(points).assertCountEquals(0)
    }

    @Test
    fun visibleCount_isReportedFromTheAvailableWidth() {
        val reported = mutableListOf<Int>()
        setChart { TestLineChart(entries = entries, onVisibleCountChanged = { reported += it }) }

        // 300 dp / 46 dp = 6.5 → 6
        assertThat(reported).containsExactly(6)
    }

    @Test
    fun visibleCount_honoursACustomPointSpacing() {
        val reported = mutableListOf<Int>()
        setChart { TestLineChart(entries = entries, pointSpacing = 30.dp, onVisibleCountChanged = { reported += it }) }

        // 300 dp / 30 dp = 10
        assertThat(reported).containsExactly(10)
    }

    // January 2023 to December 2024, oldest first.
    private val twoYears = (0 until 24).map { entry(value = 10.0 + it, year = 2023 + it / 12, month = it % 12 + 1) }

    @Test
    fun manyEntries_areAllShown_withTheAxisThinnedOut() {
        setChart { TestLineChart(entries = twoYears) }

        // 300 dp / 24 = 12.5 dp per point → a label every 2 points, counting back from December 2024.
        composeTestRule.onAllNodes(points).assertCountEquals(24)
        composeTestRule.onAllNodesWithText("12").assertCountEquals(2)
        composeTestRule.onAllNodesWithText("11").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("10").assertCountEquals(2)
    }

    @Test
    fun withoutEntryAnimation_entryChangesApplyAtOnce() {
        var shown by mutableStateOf(twoYears.takeLast(12))
        setChart { TestLineChart(entries = shown) }
        composeTestRule.mainClock.autoAdvance = false

        // Applied at once, as the paused clock doesn't send snapshot notifications.
        Snapshot.withMutableSnapshot { shown = twoYears.takeLast(6) }
        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule.onAllNodes(points).assertCountEquals(6)
    }

    @Test
    fun withEntryAnimation_removedPointsAreDropped_onceTheySlidOut() {
        var shown by mutableStateOf(twoYears.takeLast(12))
        setChart { TestLineChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500)) }

        shown = twoYears.takeLast(6)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(points).assertCountEquals(6)
        point("January 2024").assertDoesNotExist()
        composeTestRule.onNodeWithText("07").assertIsDisplayed()
        composeTestRule.onNodeWithText("06").assertDoesNotExist()
    }

    @Test
    fun withEntryAnimation_addedPointsAreShown() {
        var shown by mutableStateOf(twoYears.takeLast(6))
        setChart { TestLineChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500)) }

        shown = twoYears.takeLast(12)
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(points).assertCountEquals(12)
        point("January 2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("01").assertIsDisplayed()
    }

    @Test
    fun withEntryAnimation_pointsRescale_toTheNewLargestValue() {
        var shown by mutableStateOf(twoYears.takeLast(6))
        setChart { TestLineChart(entries = shown, entryAnimationSpec = tween(durationMillis = 500)) }

        // December 2024 goes from 33 to 56, so July 2024's 28 becomes half of the largest.
        shown = twoYears.takeLast(6).map { if (it.key == twoYears.last().key) it.copy(value = 56.0) else it }
        composeTestRule.waitForIdle()

        val july = point("July 2024").getUnclippedBoundsInRoot()
        val december = point("December 2024").getUnclippedBoundsInRoot()
        val travel = LineChartDefaults.PlotHeight.value - LineChartDefaults.PointSize.value
        assertThat((july.top - december.top).value).isWithin(1f).of(travel / 2)
    }
}
