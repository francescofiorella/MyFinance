package com.frafio.charts

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI

/** The pure sizing and angle maths behind BarChart, PieChart and ShareBar. */
class ChartGeometryTest {

    @Test
    fun maxVisibleBars_countsWholeBarsWithTheirPadding() {
        // 300 / (40 + 2 * 3) = 6.5
        assertThat(maxVisibleBars(300.dp, barWidth = 40.dp, barPadding = 3.dp)).isEqualTo(6)
        // 300 / (20 + 2 * 5) = 10
        assertThat(maxVisibleBars(300.dp, barWidth = 20.dp, barPadding = 5.dp)).isEqualTo(10)
    }

    @Test
    fun maxVisibleBars_isAtLeastOne_evenWhenNoBarFits() {
        assertThat(maxVisibleBars(10.dp, barWidth = 40.dp, barPadding = 3.dp)).isEqualTo(1)
        assertThat(maxVisibleBars(0.dp, barWidth = 40.dp, barPadding = 3.dp)).isEqualTo(1)
    }

    @Test
    fun axisLabelStep_isOne_whileSlotsFitALabel() {
        assertThat(axisLabelStep(50.dp)).isEqualTo(1)
        assertThat(axisLabelStep(20.dp)).isEqualTo(1)
    }

    @Test
    fun axisLabelStep_skipsEnoughBars_toFitALabelOnNarrowSlots() {
        // 20 / 12.5 = 1.6 → 2
        assertThat(axisLabelStep(12.5.dp)).isEqualTo(2)
        // 20 / 7 = 2.86 → 3
        assertThat(axisLabelStep(7.dp)).isEqualTo(3)
    }

    @Test
    fun axisLabelStep_withoutWidth_isOne() {
        assertThat(axisLabelStep(0.dp)).isEqualTo(1)
    }

    @Test
    fun isAxisLabelShown_stepOne_labelsEveryBar() {
        assertThat((0..5).all { isAxisLabelShown(it, latestPosition = 5, selectedPosition = 2, step = 1) }).isTrue()
    }

    @Test
    fun isAxisLabelShown_countsBackFromTheLatest() {
        val shown = (0..7).filter { isAxisLabelShown(it, latestPosition = 7, selectedPosition = 7, step = 2) }

        assertThat(shown).containsExactly(1, 3, 5, 7).inOrder()
    }

    @Test
    fun isAxisLabelShown_labelsTheSelected_andHidesItsCloseNeighbours() {
        val shown = (0..7).filter { isAxisLabelShown(it, latestPosition = 7, selectedPosition = 4, step = 2) }

        // 3 and 5 are on the step but next to the selected 4.
        assertThat(shown).containsExactly(1, 4, 7).inOrder()
    }

    @Test
    fun isAxisLabelShown_handlesBarsAfterTheLatest() {
        // A bar animating out after the latest one sits at a negative distance from it.
        assertThat(isAxisLabelShown(9, latestPosition = 7, selectedPosition = 3, step = 2)).isTrue()
        assertThat(isAxisLabelShown(8, latestPosition = 7, selectedPosition = 3, step = 2)).isFalse()
    }

    @Test
    fun stripIndexAt_onConsecutiveMonths_isTheMonthOffset() {
        val keys = listOf(10, 11, 12, 13)

        assertThat(stripIndexAt(keys, 10f)).isEqualTo(0f)
        assertThat(stripIndexAt(keys, 12.5f)).isEqualTo(2.5f)
    }

    @Test
    fun stripIndexAt_outsideTheBars_countsOneBarPerMonth() {
        val keys = listOf(10, 11, 12)

        assertThat(stripIndexAt(keys, 7f)).isEqualTo(-3f)
        assertThat(stripIndexAt(keys, 14f)).isEqualTo(4f)
    }

    @Test
    fun stripIndexAt_acrossAGap_spreadsTheMonthsOverOneBar() {
        // Bars for months 10 and 14 only: month 12 is halfway between them.
        assertThat(stripIndexAt(listOf(10, 14), 12f)).isEqualTo(0.5f)
    }

    @Test
    fun stripIndexAt_withoutBars_isZero() {
        assertThat(stripIndexAt(emptyList(), 5f)).isEqualTo(0f)
    }

    private fun bar(value: Double?) = BarChartEntry(key = 0, value = value, label = "", axisLabel = "")

    @Test
    fun shownSelection_keepsASelectedEntryWithData() {
        assertThat(shownSelection(listOf(bar(1.0), bar(2.0), bar(3.0)), selectedIndex = 1)).isEqualTo(1)
    }

    @Test
    fun shownSelection_outsideTheEntries_isTheLatestWithData() {
        val entries = listOf(bar(1.0), bar(2.0), bar(null))
        assertThat(shownSelection(entries, selectedIndex = -1)).isEqualTo(1)
        assertThat(shownSelection(entries, selectedIndex = 5)).isEqualTo(1)
    }

    @Test
    fun shownSelection_onAnEntryWithoutData_isTheLatestWithData() {
        assertThat(shownSelection(listOf(bar(null), bar(null), bar(3.0)), selectedIndex = 0)).isEqualTo(2)
    }

    @Test
    fun shownSelection_withoutData_isNone() {
        assertThat(shownSelection(listOf(bar(null), bar(null)), selectedIndex = -1)).isEqualTo(-1)
        assertThat(shownSelection(emptyList(), selectedIndex = -1)).isEqualTo(-1)
    }

    @Test
    fun arcGapDegrees_isTheArcWidthPlusOffset_asAnAngleOnTheRadius() {
        val expected = (18f / 80f) * (180f / PI.toFloat())

        assertThat(arcGapDegrees(offsetPx = 4f, arcWidthPx = 14f, radiusPx = 80f)).isEqualTo(expected)
    }

    @Test
    fun arcGapDegrees_withoutARadius_isZero() {
        assertThat(arcGapDegrees(offsetPx = 4f, arcWidthPx = 14f, radiusPx = 0f)).isEqualTo(0f)
    }

    @Test
    fun arcSweeps_shareTheCircle_minusOneGapPerPositiveArc() {
        val sweeps = arcSweeps(listOf(1.0, 3.0), gapDegrees = 10f)

        assertThat(sweeps).containsExactly(85f, 255f).inOrder()
        assertThat(sweeps.sum() + 2 * 10f).isEqualTo(360f)
    }

    @Test
    fun arcSweeps_zeroValues_getNoArcAndNoGap() {
        val sweeps = arcSweeps(listOf(1.0, 0.0, 1.0), gapDegrees = 10f)

        assertThat(sweeps).containsExactly(170f, 0f, 170f).inOrder()
    }

    @Test
    fun arcSweeps_aSingleArc_isAFullCircleWithoutGap() {
        assertThat(arcSweeps(listOf(0.0, 42.0), gapDegrees = 10f)).containsExactly(0f, 360f).inOrder()
    }

    @Test
    fun arcSweeps_nothingSpent_drawsNoArcs() {
        assertThat(arcSweeps(listOf(0.0, 0.0), gapDegrees = 10f)).containsExactly(0f, 0f).inOrder()
        assertThat(arcSweeps(emptyList(), gapDegrees = 10f)).isEmpty()
    }

    @Test
    fun shareFractions_splitTheBarByValue() {
        assertThat(shareFractions(listOf(1.0, 0.0, 3.0))).containsExactly(0.25f, 0f, 0.75f).inOrder()
    }

    @Test
    fun shareFractions_nothingSpent_areAllZero() {
        assertThat(shareFractions(listOf(0.0, 0.0))).containsExactly(0f, 0f).inOrder()
        assertThat(shareFractions(emptyList())).isEmpty()
    }

    @Test
    fun gapsAfter_onlyBetweenPositiveSegments() {
        assertThat(gapsAfter(listOf(1.0, 0.0, 2.0, 3.0, 0.0))).containsExactly(true, false, true, false, false).inOrder()
    }

    @Test
    fun gapsAfter_aSingleSegment_hasNoGap() {
        assertThat(gapsAfter(listOf(0.0, 42.0, 0.0))).containsExactly(false, false, false).inOrder()
    }
}
