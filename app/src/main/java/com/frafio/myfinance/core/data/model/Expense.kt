package com.frafio.myfinance.core.data.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frafio.myfinance.core.utils.dateToExtendedString
import com.frafio.myfinance.core.utils.dateToString
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Parcelize
@Immutable
@IgnoreExtraProperties
@Entity
@Serializable
data class Expense(
    override val name: String? = null,
    override val price: Double? = null,
    override val year: Int? = null,
    override val month: Int? = null,
    override val day: Int? = null,
    override var timestamp: Long? = null,
    override val category: Int? = null,
    override val labels: List<String> = emptyList(),
    @PrimaryKey @get:Exclude override var id: String = "$name$price$timestamp$category$labels"
) : Transaction, Parcelable {
    @Exclude
    fun getTotalId(): String {
        return "${day}_${month}_${year}"
    }

    @Exclude
    override fun getDateString(extended: Boolean): String {
        return if (extended) {
            dateToExtendedString(day, month, year)
        } else {
            dateToString(day, month, year)
        } ?: ""
    }

    @Exclude
    override fun getLocalDate(): LocalDate {
        return LocalDate.of(year!!, month!!, day!!)
    }

    @Exclude
    override fun getPriceString(showDecimal: Boolean): String {
        return if (showDecimal) {
            doubleToPrice(price!!)
        } else {
            doubleToPriceWithoutDecimals(price!!)
        }
    }
}