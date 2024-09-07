package com.frafio.myfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frafio.myfinance.utils.dateToString
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity
data class Income(
    val name: String? = null,
    val price: Double? = null,
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    var timestamp: Long? = null,
    val category: Int? = null,
    @PrimaryKey @get:Exclude var id: String = "$name$price$timestamp$category"
) {
    @Exclude
    fun getDateString(): String {
        return dateToString(day, month, year) ?: ""
    }
}