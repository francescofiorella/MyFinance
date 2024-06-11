package com.frafio.myfinance.data.models

data class Purchase(
    val email: String? = null,
    val name: String? = null,
    var price: Double? = null,
    var year: Int? = null,
    var month: Int? = null,
    var day: Int? = null,
    val type: Int? = null,
    var id: String? = null,
    var category: String? = null
) {
    fun updateID(id: String) {
        this.id = id
    }
}