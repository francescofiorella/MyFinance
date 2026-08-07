package com.frafio.myfinance.core.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.sql.Timestamp
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Currency
import java.util.Locale
import kotlin.math.round

var activeCurrencyCode by mutableStateOf("EUR")

fun doubleToString(double: Double): String {
    val locale = Locale.UK
    val nf = NumberFormat.getInstance(locale)
    val formatter = nf as DecimalFormat
    formatter.applyPattern("########0.00")

    return formatter.format(double)
}

fun doubleToStringWithoutDecimals(double: Double): String {
    val locale = Locale.UK
    val nf = NumberFormat.getInstance(locale)
    val formatter = nf as DecimalFormat
    formatter.applyPattern("########0")

    return formatter.format(double)
}

fun doubleToPrice(double: Double): String {
    val currency = Currency.getInstance(activeCurrencyCode)
    val symbol = currency.getSymbol(getLocaleFromCurrency(activeCurrencyCode))
    return "$symbol ${doubleToString(double)}"
}

fun doubleToPriceWithoutDecimals(double: Double): String {
    val currency = Currency.getInstance(activeCurrencyCode)
    val symbol = currency.getSymbol(getLocaleFromCurrency(activeCurrencyCode))
    return "$symbol ${doubleToStringWithoutDecimals(double)}"
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

@Suppress("UNUSED")
fun dateToString(date: LocalDate?): String? {
    var formattedDate: String? = null
    date?.let {
        val dayString =
            if (date.dayOfMonth < 10) "0${date.dayOfMonth}" else date.dayOfMonth.toString()
        val monthString =
            if (date.monthValue < 10) "0${date.monthValue}" else date.monthValue.toString()
        formattedDate = "$dayString/$monthString/${date.year}"
    }

    return formattedDate
}

fun dateToExtendedString(dayOfMonth: Int?, month: Int?, year: Int?): String? {
    var formattedDate: String? = null
    dayOfMonth?.let {
        month?.let {
            year?.let {
                val locale = Locale.getDefault()
                val date = LocalDate.of(year, month, dayOfMonth)
                val dayString = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()
                val monthString = date.month.getDisplayName(TextStyle.SHORT, locale)
                    .replaceFirstChar { it.uppercase() }
                formattedDate = "$dayString $monthString $year"
            }
        }
    }

    return formattedDate
}

fun dateToExtendedString(date: LocalDate?): String? {
    var formattedDate: String? = null
    date?.let {
        val locale = Locale.getDefault()
        val dayString =
            if (date.dayOfMonth < 10) "0${date.dayOfMonth}" else date.dayOfMonth.toString()
        val monthString = date.month.getDisplayName(TextStyle.SHORT, locale)
            .replaceFirstChar { it.uppercase() }
        formattedDate = "$dayString $monthString ${date.year}"
    }

    return formattedDate
}

@Suppress("UNUSED")
fun timeToString(hour: Int?, minute: Int?): String? {
    var formattedTime: String? = null
    hour?.let {
        minute?.let {
            val hourString = if (hour < 10) "0$hour" else hour.toString()
            val minuteString = if (minute < 10) "0$minute" else minute.toString()
            formattedTime = "$hourString:$minuteString"
        }
    }
    return formattedTime
}

@Suppress("UNUSED")
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0f
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun getCurrentLanguage(): String {
    return Locale.getDefault().language
}

fun Long.toUTCLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.ofOffset("UTC", ZoneOffset.UTC))

fun dateToUTCTimestamp(year: Int, month: Int, day: Int): Long {
    val localDate = LocalDate.of(year, month, day)
    val instant = localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
    return Timestamp.from(instant).time
}

fun dateToUTCTimestamp(date: LocalDate): Long {
    val instant = date.atStartOfDay().toInstant(ZoneOffset.UTC)
    return Timestamp.from(instant).time
}

fun String.capitalizeWords(): String = lowercase().split(" ").joinToString(" ") { word ->
    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun getLocaleFromCurrency(currencyCode: String): Locale {
    val countryCode = currencyCode.take(2)

    return when (currencyCode) {
        "EUR" -> Locale.GERMANY
        "FRF" -> Locale.FRANCE
        else -> Locale.getAvailableLocales().firstOrNull { locale ->
            locale.country.equals(countryCode, ignoreCase = true)
        } ?: Locale.US
    }
}