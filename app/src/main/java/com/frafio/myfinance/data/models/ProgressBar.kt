package com.frafio.myfinance.data.models

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import com.frafio.myfinance.R
import com.frafio.myfinance.utils.animateRoot

class ProgressBar(
    private val layout: ConstraintLayout,
    private val context: Context
) {
    private val frontView = layout.findViewById<View>(R.id.progress_bar_front)
    private val backView = layout.findViewById<View>(R.id.progress_bar_back)
    private val labelTextView = layout.findViewById<TextView>(R.id.percentageTV)
    private val percGuideline = layout.findViewById<Guideline>(R.id.bar_guideline)

    fun updateValue(
        value: Double,
        maxValue: Double
    ) {
        if (maxValue == 0.0) {
            frontView.visibility = View.GONE
            backView.visibility = View.GONE
            labelTextView.visibility = View.GONE
            return
        }

        frontView.visibility = View.VISIBLE
        backView.visibility = View.VISIBLE
        labelTextView.visibility = View.VISIBLE

        var percentage = value / maxValue
        val percString = "${(percentage * 100).toInt()}%"
        if (percentage > 1.0) {
            percentage = 1.0
        } else if (percentage < 0.08 && percentage != 0.0) {
            percentage = 0.08
        }
        labelTextView.text = percString
        percGuideline.setGuidelinePercent(percentage.toFloat())
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            if (percentage < 1) android.R.attr.colorPrimary else android.R.attr.colorError,
            typedValue,
            true
        )
        val color = ContextCompat.getColor(context, typedValue.resourceId)
        frontView.backgroundTintList = ColorStateList.valueOf(color)

        (layout as ViewGroup).animateRoot()
    }
}