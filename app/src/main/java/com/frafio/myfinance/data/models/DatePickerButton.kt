package com.frafio.myfinance.data.models

import android.view.View
import android.widget.AutoCompleteTextView
import androidx.fragment.app.FragmentActivity
import com.frafio.myfinance.data.enums.db.Languages
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.getCurrentLanguage
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import java.util.*

open class DatePickerButton(
    private val layout: TextInputLayout,
    private val textView: AutoCompleteTextView,
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

    var year: Int? = null
        set(value) {
            field = value
            textView.setText(dateString)
        }

    var month: Int? = null
        set(value) {
            field = value
            textView.setText(dateString)
        }

    var day: Int? = null
        set(value) {
            field = value
            textView.setText(dateString)
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
        layout.setEndIconOnClickListener(listener)
        textView.setOnClickListener(listener)
    }

    private fun removeOnClickListener() {
        layout.setEndIconOnClickListener(listener)
        textView.setOnClickListener(null)
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