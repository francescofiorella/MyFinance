package com.frafio.myfinance.core.data.model

import android.os.Parcelable
import java.time.LocalDate

interface Transaction : Parcelable {
    val name: String?
    val price: Double?
    val year: Int?
    val month: Int?
    val day: Int?
    var timestamp: Long?
    val category: Int?
    val labels: List<String>
    val id: String

    fun getDateString(extended: Boolean = false): String
    fun getPriceString(showDecimal: Boolean = true): String
    fun getLocalDate(): LocalDate
}