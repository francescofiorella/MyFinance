package com.frafio.charts

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.toPath
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

/** The morph from [start] to [end], drawn with a path and matrix reused across frames. */
internal class ShapeMorph(start: RoundedPolygon, end: RoundedPolygon) {
    private val morph = Morph(start, end)
    private val path = Path()
    private val matrix = Matrix()

    /**
     * Draws the morph at [progress] (kept within 0..1) in [color]: its unit-sized polygons scaled to [size]
     * and centred on [center], as Material's RoundedPolygon.toShape() does.
     */
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun draw(drawScope: DrawScope, progress: Float, size: Size, center: Offset, color: Color) {
        morph.toPath(progress = progress.coerceIn(0f, 1f), path = path)
        matrix.reset()
        matrix.scale(size.width, size.height)
        path.transform(matrix)
        path.translate(center - path.getBounds().center)
        drawScope.drawPath(path, color)
    }
}
