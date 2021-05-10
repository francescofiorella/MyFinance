package com.frafio.myfinance.data.models

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

data class Purchase(
    val email: String? = null,
    val name: String? = null,
    var price: Double? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val type: Int? = null,
    var id: String? = null,
    var formattedDate: String? = null,
    var formattedPrice: String? = null
) {
    fun updateFormattedDate() {
        day?.let { day ->
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
    }

    fun updateFormattedPrice() {
        price?.let { price ->
            val locale = Locale("en", "UK")
            val nf = NumberFormat.getInstance(locale)
            val formatter = nf as DecimalFormat
            formatter.applyPattern("###,###,##0.00")
            formattedPrice = "€ ${formatter.format(price)}"
        }
    }
}