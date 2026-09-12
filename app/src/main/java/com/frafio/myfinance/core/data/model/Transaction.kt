package com.frafio.myfinance.core.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Keep
@Serializable
sealed interface Transaction : Parcelable {
    val name: String?
    val price: Double?
    val year: Int?
    val month: Int?
    val day: Int?
    var timestamp: Long?
    val category: Int?
    val labels: List<String>
    val updatedAt: Long?
    @get:PropertyName("isDeleted")
    val isDeleted: Boolean?
    val deleteAt: Long?
    val id: String

    fun getDateString(extended: Boolean = false): String
    fun getPriceString(showDecimal: Boolean = true): String
    fun getLocalDate(): LocalDate
}