package com.frafio.myfinance.data.models

import com.frafio.myfinance.utils.formatPrice

data class ReceiptItem(
    val name: String? = null,
    val price: Double? = null,
    var id: String? = null,
    var formattedPrice: String? = null
) {
    fun updateFormattedPrice() {
        price?.let { price ->
            formattedPrice = "€ ${formatPrice(price)}"
        }
    }
}