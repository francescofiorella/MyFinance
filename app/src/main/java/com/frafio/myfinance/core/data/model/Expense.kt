package com.frafio.myfinance.core.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frafio.myfinance.core.utils.dateToExtendedString
import com.frafio.myfinance.core.utils.dateToString
import com.frafio.myfinance.core.utils.doubleToPrice
import com.frafio.myfinance.core.utils.doubleToPriceWithoutDecimals
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Keep
@Parcelize
@Immutable
@IgnoreExtraProperties
@Entity
@Serializable
data class Expense(
    override val name: String = "",
    override val price: Double = 0.0,
    override val year: Int = 0,
    override val month: Int = 0,
    override val day: Int = 0,
    override var timestamp: Long = 0L,
    override val category: Int = -1,
    override val labels: List<String> = emptyList(),
    override val updatedAt: Long? = null,
    @get:PropertyName("isDeleted") override val isDeleted: Boolean? = null,
    override val deleteAt: Long? = null,
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
        return LocalDate.of(year, month, day)
    }

    @Exclude
    override fun getPriceString(showDecimal: Boolean): String {
        return if (showDecimal) {
            doubleToPrice(price)
        } else {
            doubleToPriceWithoutDecimals(price)
        }
    }
}