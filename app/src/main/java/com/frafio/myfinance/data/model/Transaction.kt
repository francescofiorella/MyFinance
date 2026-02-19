package com.frafio.myfinance.data.model

import java.time.LocalDate

interface Transaction {
    val name: String?
    val price: Double?
    val year: Int?
    val month: Int?
    val day: Int?
    var timestamp: Long?
    val category: Int?
    val id: String

    fun getDateString(extended: Boolean = false): String
    fun getPriceString(showDecimal: Boolean = true): String
    fun getLocalDate(): LocalDate
}