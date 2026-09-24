package com.frafio.myfinance.core.components

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI

/** The pure sizing and angle maths behind BarChart and PieChart. */
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
}
