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
import androidx.core.content.res.ResourcesCompat
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

fun ViewGroup.animateRoot(duration: Long = 100) {
    val transition = AutoTransition()
    transition.duration = duration
    TransitionManager.beginDelayedTransition(this, transition)
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