package com.frafio.myfinance.data.models

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
)