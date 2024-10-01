package com.frafio.myfinance.data.widget

import android.view.View
import androidx.core.util.Pair
import androidx.fragment.app.FragmentActivity
import com.frafio.myfinance.data.enums.db.Languages
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.getCurrentLanguage
import com.frafio.myfinance.utils.toUTCLocalDateTime
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

open class DatePickerRangeButton(
    layout: View,
    private val context: FragmentActivity
) {
    companion object {
        private const val DATE_PICKER_TAG: String = "DATE_PICKER"
        private val DATE_PICKER_TITLE: String = when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Select dates"
            Languages.ITALIANO.value -> "Seleziona date"
            else -> "Select dates"
        }
    }

    val listener = View.OnClickListener {
        onStart()
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText(DATE_PICKER_TITLE)
        val materialDatePicker = builder.build()
        showDatePicker(materialDatePicker)
    }

    var startDate: LocalDate? = null
    val startDateString: String?
        get() = dateToString(startDate?.dayOfMonth, startDate?.monthValue, startDate?.year)
    var endDate: LocalDate? = null
    val endDateString: String?
        get() = dateToString(endDate?.dayOfMonth, endDate?.monthValue, endDate?.year)

    init {
        layout.setOnClickListener(listener)
    }


    private fun showDatePicker(materialDatePicker: MaterialDatePicker<*>) {
        if (!materialDatePicker.isAdded) {
            materialDatePicker.show(context.supportFragmentManager, DATE_PICKER_TAG)
            materialDatePicker.addOnPositiveButtonClickListener { selection ->
                // get selected date
                val dateSelection = selection as? Pair<*, *>
                val startSelection = Date(
                    dateSelection!!.first.toString().toLong().toUTCLocalDateTime()
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                val endSelection = Date(
                    dateSelection.second.toString().toLong().toUTCLocalDateTime()
                        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                val calendar = Calendar.getInstance()

                calendar.time = startSelection
                startDate = LocalDate.of(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )
                calendar.time = endSelection
                endDate = LocalDate.of(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH] + 1,
                    calendar[Calendar.DAY_OF_MONTH]
                )

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