package com.frafio.myfinance.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.round

fun doubleToString(double: Double): String {
    val locale = Locale("en", "UK")
    val nf = NumberFormat.getInstance(locale)
    val formatter = nf as DecimalFormat
    formatter.applyPattern("###,###,##0.00")

    return formatter.format(double)
}

fun doubleToStringWithoutDecimals(double: Double): String {
    val locale = Locale("en", "UK")
    val nf = NumberFormat.getInstance(locale)
    val formatter = nf as DecimalFormat
    formatter.applyPattern("###,###,##0")

    return formatter.format(double)
}

fun doubleToPrice(double: Double): String {
    return "€ ${doubleToString(double)}"
}

fun doubleToPriceWithoutDecimals(double: Double): String {
    return "€ ${doubleToStringWithoutDecimals(double)}"
}

fun dateToString(dayOfMonth: Int?, month: Int?, year: Int?): String? {
    var formattedDate: String? = null
    dayOfMonth?.let { day ->
        month?.let { month ->
            year?.let { year ->
                val dayString: String = if (day < 10) {
                    "0$day"
                } else {
                    day.toString()
                }
                val monthString: String = if (month < 10) {
                    "0$month"
                } else {
                    month.toString()
                }
                formattedDate = "$dayString/$monthString/$year"
            }
        }
    }

    return formattedDate
}

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0f
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}