package com.frafio.myfinance.core.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin

/** A ring segment with rounded ends, centred in the layout, for clipping a pie chart arc. */
internal class ArcShape(
    private val startAngle: Float,
    private val sweepAngle: Float,
    private val strokeWidthPx: Float,
    private val radiusPx: Float
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
}
