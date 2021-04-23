package com.frafio.lamiafinanza.models

data class Purchase(
    val name: String? = null,
    val email: String? = null,
    val price: Double? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val type: Int? = null
)