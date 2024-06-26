package com.frafio.myfinance.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.frafio.myfinance.R
import com.google.android.material.color.DynamicColors
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries

fun View.instantShow() {
    visibility = View.VISIBLE
}

fun View.instantHide() {
    visibility = View.GONE
}

fun TextView.clearText() {
    text = ""
}

fun ViewGroup.animateRoot(duration: Long = 100) {
    val transition = AutoTransition()
    transition.duration = duration
    TransitionManager.beginDelayedTransition(this, transition)
}

fun ValueLineChart.setValueLineChartData(list: List<Pair<String, Double>>, dynamicColors: Boolean) {
    ValueLineSeries().also { series ->
        val colorPrimaryContainer: Int
        if (dynamicColors) {
            // if your base context is already using Material3 theme you can omit R.style argument
            val dynamicColorContext = DynamicColors.wrapContextIfAvailable(
                context,
                R.style.ThemeOverlay_Material3_DynamicColors_DayNight
            )
            // define attributes to resolve in an array
            val attrsToResolve = intArrayOf(
                R.attr.colorPrimaryContainer, // 0
            )
            // now resolve them
            val ta = dynamicColorContext.obtainStyledAttributes(attrsToResolve)
            colorPrimaryContainer = ta.getColor(0, 0)
            ta.recycle() // recycle TypedArray
        } else {
            colorPrimaryContainer = ContextCompat.getColor(
                context,
                R.color.md_theme_primaryContainer
            )
        }
        series.color = ColorUtils
            .setAlphaComponent(
                colorPrimaryContainer, 200
            )

        list.forEach { pair ->
            val date: String = pair.first
            // convert to Float e round the the second decimal figure
            val value: Float = pair.second.toFloat().round(2)
            series.addPoint(ValueLinePoint(date, value))
        }

        addSeries(series)
    }
}

fun createTextDrawable(context: Context, text: String): Drawable {
    val typeface = ResourcesCompat.getFont(context, R.font.nunito_bold)
    val typedValue = TypedValue()
    context.theme.resolveAttribute(
        com.google.android.material.R.attr.colorOnSecondaryContainer,
        typedValue,
        true
    )
    val paint = Paint()
    paint.color = typedValue.data
    paint.textSize = 104f
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = typeface
    paint.isAntiAlias = true

    val bounds = Rect()
    paint.getTextBounds(text, 0, text.length, bounds)

    val bitmapSize = 96
    val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawText(
        text,
        bitmapSize / 2f,
        bitmapSize / 2f - (paint.ascent() + paint.descent()) / 2,
        paint
    )

    return BitmapDrawable(context.resources, bitmap)
}