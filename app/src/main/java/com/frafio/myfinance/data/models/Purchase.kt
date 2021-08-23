package com.frafio.myfinance.data.models

import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.doubleToString

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

    private var _formattedPrice: String? = null
    val formattedPrice: String?
        get() = _formattedPrice

    fun updatePurchase(
        id: String? = null,
        updateDate: Boolean = true,
        updatePrice: Boolean = true
    ) {
        id?.let {
            this.id = it
        }

        if (updateDate) {
            updateFormattedDate()
        }

        if (updatePrice) {
            updateFormattedPrice()
        }
    }

    private fun updateFormattedDate() {
        _formattedDate = dateToString(day, month, year)
    }

    private fun updateFormattedPrice() {
        price?.let { price ->
            _formattedPrice = "€ ${doubleToString(price)}"
        }
    }
}