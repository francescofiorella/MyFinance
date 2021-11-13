package com.frafio.myfinance.data.models

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.frafio.myfinance.R
import com.frafio.myfinance.utils.dateToString
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.util.*

open class DatePickerButton(
    private val layout: MaterialCardView,
    private val textView: TextView,
    private val context: FragmentActivity
) {
    companion object {
        private const val DATE_PICKER_TITLE: String = "Seleziona una data"
        private const val DATE_PICKER_TAG: String = "DATE_PICKER"
    }

    var isEnabled: Boolean = true
        set(value) {
            field = value

            if (value) {
                setOnClickListener()
            } else {
                removeOnClickListener()
                textView.setTextColor(ContextCompat.getColor(context, R.color.disabled_text))
            }
        }

    var year: Int? = null
        set(value) {
            field = value
            textView.text = dateString
        }

    var month: Int? = null
        set(value) {
            field = value
            textView.text = dateString
        }

    var day: Int? = null
        set(value) {
            field = value
            textView.text = dateString
        }

    val dateString: String?
        get() {
            return dateToString(day, month, year)
        }

    init {
        year = LocalDate.now().year
        month = LocalDate.now().monthValue
        day = LocalDate.now().dayOfMonth

        setOnClickListener()
    }

    private fun setOnClickListener() {
        layout.setOnClickListener {
            // date picker
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.clear()
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText(DATE_PICKER_TITLE)
            builder.setSelection(today)
            val materialDatePicker = builder.build()

            showDatePicker(materialDatePicker)
        }
    }

    private fun removeOnClickListener() {
        layout.setOnClickListener(null)
    }

    private fun showDatePicker(materialDatePicker: MaterialDatePicker<*>) {
        if (!materialDatePicker.isAdded) {
            materialDatePicker.show(context.supportFragmentManager, DATE_PICKER_TAG)
            materialDatePicker.addOnPositiveButtonClickListener { selection ->
                // get selected date
                val date = Date(selection.toString().toLong())
                val calendar = Calendar.getInstance()

                calendar.time = date

                year = calendar[Calendar.YEAR]
                month = calendar[Calendar.MONTH] + 1
                day = calendar[Calendar.DAY_OF_MONTH]

                onPositiveBtnClickListener()
            }
        }
    }

    open fun onPositiveBtnClickListener() {}
}