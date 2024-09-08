package com.frafio.myfinance.data.widget

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.frafio.myfinance.data.enums.db.Languages
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.getCurrentLanguage
import com.frafio.myfinance.utils.toLocalDateTime
import com.frafio.myfinance.utils.toUTCLocalDateTime
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date

open class DatePickerButton(
    layout: ViewGroup,
    private val textView: TextView,
    private val context: FragmentActivity
) {
    companion object {
        private const val DATE_PICKER_TAG: String = "DATE_PICKER"
        private val DATE_PICKER_TITLE: String = when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Select date"
            Languages.ITALIANO.value -> "Seleziona data"
            else -> "Select date"
        }
    }

    val listener = View.OnClickListener {
        onStart()
        textView.requestFocus()
        // date picker
        val calendar = Calendar.getInstance()
        if (year != null && month != null && day != null) {
            calendar.set(year!!, month!! - 1, day!!)
        } else {
            LocalDate.now().apply {
                calendar.set(year, monthValue, dayOfMonth)
            }
        }
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText(DATE_PICKER_TITLE)
        builder.setSelection(calendar.timeInMillis.toLocalDateTime().atZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC)).toInstant().toEpochMilli())
        val materialDatePicker = builder.build()
        showDatePicker(materialDatePicker)
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
        LocalDate.now().let {
            year = it.year
            month = it.monthValue
            day = it.dayOfMonth
        }

        layout.setOnClickListener(listener)
        textView.setOnClickListener(listener)
    }


    private fun showDatePicker(materialDatePicker: MaterialDatePicker<*>) {
        if (!materialDatePicker.isAdded) {
            materialDatePicker.show(context.supportFragmentManager, DATE_PICKER_TAG)
            materialDatePicker.addOnPositiveButtonClickListener { selection ->
                // get selected date
                val date = Date(selection.toString().toLong().toUTCLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                val calendar = Calendar.getInstance()

                calendar.time = date

                year = calendar[Calendar.YEAR]
                month = calendar[Calendar.MONTH] + 1
                day = calendar[Calendar.DAY_OF_MONTH]

                onPositiveBtnClickListener()
            }
        }
    }

    open fun onPositiveBtnClickListener() {
        // Should override this method to get the selected date
    }

    open fun onStart() {
        // Should override this method to perform any preparation operation
    }
}