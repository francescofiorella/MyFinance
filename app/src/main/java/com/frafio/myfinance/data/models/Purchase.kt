package com.frafio.myfinance.data.models

import com.frafio.myfinance.utils.dateToString
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate

@IgnoreExtraProperties
data class Purchase(
    val name: String? = null,
    val price: Double? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    var timestamp: Long? = null,
    val category: Int? = null,
    @Exclude var id: String? = null
) {
    @Exclude
    fun getTotalId(): String {
        return "${day}_${month}_${year}"
    }

    @Exclude
    fun getDateString(): String {
        return dateToString(day, month, year) ?: ""
    }

    @Exclude
    fun getLocalDate(): LocalDate {
        return LocalDate.of(year!!, month!!, day!!)
    }
}