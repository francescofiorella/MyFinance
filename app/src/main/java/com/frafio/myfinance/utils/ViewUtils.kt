package com.frafio.myfinance.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.frafio.myfinance.R

fun View.instantShow() {
    visibility = View.VISIBLE
}

fun View.instantHide() {
    visibility = View.GONE
}

fun TextView.clearText() {
    text = ""
}

fun ViewGroup.animateRoot(duration: Long? = 200) {
    val transition = AutoTransition()
    duration?.let { transition.duration = it }
    TransitionManager.beginDelayedTransition(this, transition)
}

fun Context.getThemeColor(@AttrRes attrResId: Int): Int {
    val typedValue = TypedValue()
    val resolved = theme.resolveAttribute(attrResId, typedValue, true)
    return if (resolved) typedValue.data else throw IllegalArgumentException("Attribute not found")
}

fun Context.createTextDrawable(text: String): Drawable {
    val typeface = ResourcesCompat.getFont(applicationContext, R.font.nunito_bold)
    val typedValue = TypedValue()
    theme.resolveAttribute(
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
    val bitmap = createBitmap(bitmapSize, bitmapSize)
    val canvas = Canvas(bitmap)
    canvas.drawText(
        text,
        bitmapSize / 2f,
        bitmapSize / 2f - (paint.ascent() + paint.descent()) / 2,
        paint
    )

    return bitmap.toDrawable(resources)
}