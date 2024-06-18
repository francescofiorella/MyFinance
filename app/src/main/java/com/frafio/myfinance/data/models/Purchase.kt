package com.frafio.myfinance.data.models

import com.frafio.myfinance.utils.dateToString
import java.time.LocalDate

data class Purchase(
    val email: String? = null,
    val name: String? = null,
    val price: Double? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val type: Int? = null,
    var id: String? = null,
    val category: String? = null
) {
    fun updateID(id: String) {
        this.id = id
    }

    fun getTotalId(): String {
        return "${day}_${month}_${year}"
    }

    fun getDateString(): String {
        return dateToString(day, month, year) ?: ""
    }

    fun getLocalDate(): LocalDate {
        return LocalDate.of(year!!, month!!, day!!)
    }
}