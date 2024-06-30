package com.frafio.myfinance.data.models

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.frafio.myfinance.R
import com.frafio.myfinance.utils.animateRoot

class ProgressBar(
    private val layout: ConstraintLayout,
    private val context: Context
) {
    private val frontView = layout.findViewById<View>(R.id.progress_bar_front)
    private val backView = layout.findViewById<View>(R.id.progress_bar_back)
    private val labelTextView = layout.findViewById<TextView>(R.id.percentageTV)

    fun updateValue(
        value: Double,
        maxValue: Double
    ) {
        if (maxValue == 0.0) {
            frontView.visibility = View.GONE
            labelTextView.visibility = View.GONE
            return
        }

        val newWidth: Int
        val color: Int

        frontView.visibility = View.VISIBLE
        labelTextView.visibility = View.VISIBLE

        val percentage = value / maxValue
        val percString = "${(percentage * 100).toInt()}%"
        labelTextView.text = percString
        if (percentage <= 1) {
            val nw = (backView.width * percentage).toInt()
            newWidth = if (nw == 0) {
                frontView.visibility = View.INVISIBLE
                1
            } else {
                nw
            }
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.colorPrimary,
                typedValue,
                true
            )
            color = ContextCompat.getColor(context, typedValue.resourceId)
        } else {
            newWidth = backView.width
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.colorError,
                typedValue,
                true
            )
            color = ContextCompat.getColor(context, typedValue.resourceId)
        }
        frontView.updateLayoutParams<ViewGroup.LayoutParams> {
            width = newWidth
        }
        frontView.backgroundTintList = ColorStateList.valueOf(color)

        (layout as ViewGroup).animateRoot()
    }
}