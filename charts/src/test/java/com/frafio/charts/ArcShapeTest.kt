package com.frafio.charts

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.math.asin
import kotlin.math.sin

/** The cornered ends against the round ones they morph from, on a 14 px arc of radius 80 centred at (100, 100). */
@RunWith(RobolectricTestRunner::class)
class ArcShapeTest {

    private fun bounds(startAngle: Float, sweepAngle: Float, cornerFraction: Float): Rect {
        val outline = ArcShape(startAngle, sweepAngle, strokeWidthPx = 14f, radiusPx = 80f, cornerFraction = cornerFraction)
            .createOutline(Size(200f, 200f), LayoutDirection.Ltr, Density(1f))
        return (outline as Outline.Generic).path.getBounds()
    }

    @Test
    fun boundsAreTheShapesOwn() {
        // The round start end is centred at -60° on the radius, so the top is its centre's y minus its radius.
        val expectedTop = 100f + 80f * sin(Math.toRadians(-60.0)).toFloat() - 7f

        assertThat(bounds(-60f, 120f, cornerFraction = 1f).top).isWithin(0.05f).of(expectedTop)
    }

    @Test
    fun cornersJustBelowFullyRound_matchTheRoundEnds() {
        val round = bounds(-60f, 120f, cornerFraction = 1f)
        val cornered = bounds(-60f, 120f, cornerFraction = 0.999f)

        assertThat(cornered.left).isWithin(0.05f).of(round.left)
        assertThat(cornered.top).isWithin(0.05f).of(round.top)
        assertThat(cornered.right).isWithin(0.05f).of(round.right)
        assertThat(cornered.bottom).isWithin(0.05f).of(round.bottom)
    }

    @Test
    fun squarerEnds_keepTheTipOfTheRoundEnds() {
        // From the bottom up to an end whose round tip just touches the horizontal line through the centre.
        val tip = Math.toDegrees(asin(7.0 / 80.0)).toFloat()
        val sweep = 90f - tip

        assertThat(bounds(90f, sweep, cornerFraction = 1f).top).isWithin(0.05f).of(100f)
        assertThat(bounds(90f, sweep, cornerFraction = 0.3f).top).isWithin(0.05f).of(100f)
    }
}
