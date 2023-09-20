package com.frafio.myfinance.utils

import com.frafio.myfinance.R
import com.frafio.myfinance.Strings
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import kotlin.math.round

fun doubleToString(double: Double): String {
    val locale = Locale("en", "UK")
    val nf = NumberFormat.getInstance(locale)
    val formatter = nf as DecimalFormat
    formatter.applyPattern("########0.00")

    return formatter.format(double)
}

fun doubleToStringWithoutDecimals(double: Double): String {
    val locale = Locale("en", "UK")
    val nf = NumberFormat.getInstance(locale)
    val formatter = nf as DecimalFormat
    formatter.applyPattern("########0")

    return formatter.format(double)
}

fun doubleToPrice(double: Double): String {
    return "${Strings.get(R.string.currency)} ${doubleToString(double)}" // € edited to $
}

fun doubleToPriceWithoutDecimals(double: Double): String {
    return "${Strings.get(R.string.currency)} ${doubleToStringWithoutDecimals(double)}" // € edited to $
}

fun dateToString(dayOfMonth: Int?, month: Int?, year: Int?): String? {
    var formattedDate: String? = null
    dayOfMonth?.let {
        month?.let {
            year?.let {
                val dayString = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                val monthString = if (month < 10) "0$month" else month.toString()
                formattedDate = "$dayString/$monthString/$year"
            }
        }
    }

    return formattedDate
}

/*fun timeToString(hour: Int?, minute: Int?): String? {
    var formattedTime: String? = null
    hour?.let {
        minute?.let {
            val hourString = if (hour < 10) "0$hour" else hour.toString()
            val minuteString = if (minute < 10) "0$minute" else minute.toString()
            formattedTime = "$hourString:$minuteString"
        }
    }
    return formattedTime
}*/

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0f
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun getCurrentLanguage(): String {
    return Locale.getDefault().language
}

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun Long.toUTCLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.ofOffset("UTC", ZoneOffset.UTC))