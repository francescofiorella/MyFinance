package com.frafio.myfinance.data.models

import com.frafio.myfinance.utils.formatDate
import com.frafio.myfinance.utils.formatPrice

data class Purchase(
    val email: String? = null,
    val name: String? = null,
    var price: Double? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val type: Int? = null,
    var id: String? = null
) {
    private var _formattedDate: String? = null
    val formattedDate: String?
        get() = _formattedDate

    private var privateFormattedPrice: String? = null
    val formattedPrice: String?
        get() = privateFormattedPrice

    fun updatePurchase(id: String? = null, date: Boolean = true, price: Boolean = true) {
        id?.let {
            this.id = it
        }

        if (date) {
            updateFormattedDate()
        }

        if (price) {
            updateFormattedPrice()
        }
    }

    private fun updateFormattedDate() {
        _formattedDate = formatDate(day, month, year)
    }

    private fun updateFormattedPrice() {
        price?.let { price ->
            privateFormattedPrice = "€ ${formatPrice(price)}"
        }
    }
}