package com.frafio.myfinance.data.models

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

data class ReceiptItem(
    val name: String? = null,
    val price: Double? = null,
    var id: String? = null,
    var formattedPrice: String? = null
) {
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