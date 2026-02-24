package com.frafio.myfinance.data.widget

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import com.frafio.myfinance.R
import com.frafio.myfinance.utils.animateRoot
import com.frafio.myfinance.utils.doubleToPrice
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class BarChart(
    private val layout: ConstraintLayout,
    private val context: Context
) {
    private var selectedBarIndex = 0

    private val barLayouts: List<View> = mutableListOf(
        layout.findViewById(R.id.bar_0_layout),
        layout.findViewById(R.id.bar_1_layout),
        layout.findViewById(R.id.bar_2_layout),
        layout.findViewById(R.id.bar_3_layout),
        layout.findViewById(R.id.bar_4_layout),
        layout.findViewById(R.id.bar_5_layout),
        layout.findViewById(R.id.bar_6_layout),
        layout.findViewById(R.id.bar_7_layout),
        layout.findViewById(R.id.bar_8_layout),
        layout.findViewById(R.id.bar_9_layout),
        layout.findViewById(R.id.bar_10_layout),
        layout.findViewById(R.id.bar_11_layout)

    )

    private val barViews: List<View> = mutableListOf(
        layout.findViewById(R.id.bar_0),
        layout.findViewById(R.id.bar_1),
        layout.findViewById(R.id.bar_2),
        layout.findViewById(R.id.bar_3),
        layout.findViewById(R.id.bar_4),
        layout.findViewById(R.id.bar_5),
        layout.findViewById(R.id.bar_6),
        layout.findViewById(R.id.bar_7),
        layout.findViewById(R.id.bar_8),
        layout.findViewById(R.id.bar_9),
        layout.findViewById(R.id.bar_10),
        layout.findViewById(R.id.bar_11)
    )
    private val labelsTextViews: List<TextView> = mutableListOf(
        layout.findViewById(R.id.bar_0_label),
        layout.findViewById(R.id.bar_1_label),
        layout.findViewById(R.id.bar_2_label),
        layout.findViewById(R.id.bar_3_label),
        layout.findViewById(R.id.bar_4_label),
        layout.findViewById(R.id.bar_5_label),
        layout.findViewById(R.id.bar_6_label),
        layout.findViewById(R.id.bar_7_label),
        layout.findViewById(R.id.bar_8_label),
        layout.findViewById(R.id.bar_9_label),
        layout.findViewById(R.id.bar_10_label),
        layout.findViewById(R.id.bar_11_label)
    )
    private val indicatorTextView = layout.findViewById<TextView>(R.id.indicatorTV)

    private val placeHolderHeight = 294

    private var labels = listOf<String>()
    private var values = listOf<Double>()

    init {
        val newLabels = mutableListOf<String>()
        var currentDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        for (view in labelsTextViews) {
            val label = if (currentDate.monthValue > 9) {
                "${currentDate.monthValue}"
            } else {
                "0${currentDate.monthValue}"
            }

            newLabels.add("${label}/${currentDate.year - 2000}")

            view.text = label
            currentDate = currentDate.minusMonths(1)
        }
        for (i in barLayouts.indices) {
            barLayouts[i].setOnClickListener {
                selectBar(i)
            }
        }
        labels = newLabels
    }

    fun updateValues(
        newLabels: List<String>,
        newValues: List<Double>
    ) {
        labels = newLabels
        values = newValues
        val maxValue = values.max()
        for (i in values.indices) {
            var newHeight = if (values[i] != 0.0) {
                barViews[i].visibility = View.VISIBLE
                (values[i] * placeHolderHeight / maxValue).toInt()
            } else {
                barViews[i].visibility = View.INVISIBLE
                1
            }
            if (newHeight == 0) newHeight = 1
            barViews[i].updateLayoutParams<ViewGroup.LayoutParams> {
                height = newHeight
            }
            labelsTextViews[i].text = labels[i].substring(0, 2)
        }

        // set the selected view appearance
        selectBar(selectedBarIndex)

        // init animation
        (layout as ViewGroup).animateRoot()
    }

    private fun selectBar(index: Int) {
        // Deselect previous bar
        barViews[selectedBarIndex].isSelected = false
        labelsTextViews[selectedBarIndex].text = labels[selectedBarIndex].substring(0, 2)
        var typeface = ResourcesCompat.getFont(context, R.font.nunito)
        labelsTextViews[selectedBarIndex].typeface = typeface
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.colorControlNormal,
            typedValue,
            true
        )
        labelsTextViews[selectedBarIndex].setTextColor(
            ContextCompat.getColor(context, typedValue.resourceId)
        )

        // Select new bar
        barViews[index].isSelected = true
        labelsTextViews[index].text = labels[index]
        typeface = ResourcesCompat.getFont(context, R.font.nunito_semibold)
        labelsTextViews[index].typeface = typeface
        labelsTextViews[index].setTextColor(
            ContextCompat.getColor(context, R.color.md_theme_onSurface)
        )
        indicatorTextView.visibility = View.VISIBLE
        indicatorTextView.text = doubleToPrice(values[index])
        indicatorTextView.background = ContextCompat.getDrawable(
            context,
            if (index < 6) R.drawable.bg_round_popup_right else R.drawable.bg_round_popup_left
        )
        indicatorTextView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            bottomToTop = barViews[index].id
            if (index < 6) {
                startToStart = ConstraintLayout.LayoutParams.UNSET
                endToEnd = barViews[index].id
            } else {
                startToStart = barViews[index].id
                endToEnd = ConstraintLayout.LayoutParams.UNSET
            }
        }
        // cambia constraints indicator
        selectedBarIndex = index
    }
}