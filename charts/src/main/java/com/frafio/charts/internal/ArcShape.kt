package com.frafio.charts.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

/**
 * A ring segment centred in the layout, for clipping a pie chart arc; [roundedEnds] false gives straight radial ends.
 * Rounded ends below a [cornerFraction] of 1 square off, keeping their tip, with corners that fraction of fully round.
 */
internal class ArcShape(
    private val startAngle: Float,
    private val sweepAngle: Float,
    private val strokeWidthPx: Float,
    private val radiusPx: Float,
    private val roundedEnds: Boolean = true,
    private val cornerFraction: Float = 1f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        if (sweepAngle <= 0f) return Outline.Generic(path)

        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = radiusPx + strokeWidthPx / 2f
        val innerRadius = radiusPx - strokeWidthPx / 2f

        val outerRect = Rect(center.x - outerRadius, center.y - outerRadius, center.x + outerRadius, center.y + outerRadius)
        val innerRect = Rect(center.x - innerRadius, center.y - innerRadius, center.x + innerRadius, center.y + innerRadius)

        // Use a slightly less than 360 value to avoid path closing issues with rounded caps
        val effectiveSweep = sweepAngle.coerceIn(0f, 359.99f)

        val endAngle = startAngle + effectiveSweep
        if (!roundedEnds) {
            // arcTo joins the two arcs with the radial edges.
            path.arcTo(outerRect, startAngle, effectiveSweep, true)
            path.arcTo(innerRect, endAngle, -effectiveSweep, false)
            path.close()
            return Outline.Generic(path)
        }

        // A full ring shows no ends, and its corner arcs would sweep past 360°.
        if (cornerFraction < 1f && effectiveSweep < 359f) {
            addCorneredSegment(path, center, outerRect, innerRect, effectiveSweep)
            return Outline.Generic(path)
        }

        val startAngleRad = Math.toRadians(startAngle.toDouble())
        val endAngleRad = Math.toRadians(endAngle.toDouble())

        val startCapCenter = Offset(
            center.x + radiusPx * cos(startAngleRad).toFloat(),
            center.y + radiusPx * sin(startAngleRad).toFloat()
        )
        val endCapCenter = Offset(
            center.x + radiusPx * cos(endAngleRad).toFloat(),
            center.y + radiusPx * sin(endAngleRad).toFloat()
        )
        val capRadius = strokeWidthPx / 2f
        val startCapRect = Rect(startCapCenter.x - capRadius, startCapCenter.y - capRadius, startCapCenter.x + capRadius, startCapCenter.y + capRadius)
        val endCapRect = Rect(endCapCenter.x - capRadius, endCapCenter.y - capRadius, endCapCenter.x + capRadius, endCapCenter.y + capRadius)

        path.arcTo(outerRect, startAngle, effectiveSweep, true)
        path.arcTo(endCapRect, endAngle, 180f, false)
        path.arcTo(innerRect, endAngle, -effectiveSweep, false)
        path.arcTo(startCapRect, startAngle + 180f, 180f, false)
        path.close()

        return Outline.Generic(path)
    }

    /**
     * Each end is a radial edge at the round end's tip, joined to the outer and inner arcs by corners of radius c,
     * each tangent to its arc and to the edge. At c equal to half the stroke both corners meet the round end.
     */
    private fun addCorneredSegment(path: Path, center: Offset, outerRect: Rect, innerRect: Rect, sweep: Float) {
        val outerRadius = radiusPx + strokeWidthPx / 2f
        val innerRadius = radiusPx - strokeWidthPx / 2f
        val capRadius = strokeWidthPx / 2f
        val c = (capRadius * cornerFraction).coerceIn(0.01f, capRadius)

        val tip = degreesAsin(capRadius / radiusPx)
        val outerTurn = degreesAsin(c / (outerRadius - c))
        val innerTurn = degreesAsin(c / (innerRadius + c))
        val endEdge = startAngle + sweep + tip
        val startEdge = startAngle - tip

        path.arcTo(outerRect, startEdge + outerTurn, sweep + 2 * (tip - outerTurn), true)
        path.arcTo(Rect(center.polar(outerRadius - c, endEdge - outerTurn), c), endEdge - outerTurn, 90f + outerTurn, false)
        path.arcTo(Rect(center.polar(innerRadius + c, endEdge - innerTurn), c), endEdge + 90f, 90f - innerTurn, false)
        path.arcTo(innerRect, endEdge - innerTurn, -(sweep + 2 * (tip - innerTurn)), false)
        path.arcTo(Rect(center.polar(innerRadius + c, startEdge + innerTurn), c), startEdge + innerTurn + 180f, 90f - innerTurn, false)
        path.arcTo(Rect(center.polar(outerRadius - c, startEdge + outerTurn), c), startEdge - 90f, 90f + outerTurn, false)
        path.close()
    }
}

private fun degreesAsin(x: Float): Float = Math.toDegrees(asin(x.coerceIn(-1f, 1f).toDouble())).toFloat()

private fun Offset.polar(radius: Float, degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return Offset(x + radius * cos(radians).toFloat(), y + radius * sin(radians).toFloat())
}
