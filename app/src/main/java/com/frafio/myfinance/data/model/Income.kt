package com.frafio.myfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frafio.myfinance.utils.dateToExtendedString
import com.frafio.myfinance.utils.dateToString
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate

@IgnoreExtraProperties
@Entity
data class Income(
    override val name: String? = null,
    override val price: Double? = null,
    override val year: Int? = null,
    override val month: Int? = null,
    override val day: Int? = null,
    override var timestamp: Long? = null,
    override val category: Int? = null,
    @PrimaryKey @get:Exclude override var id: String = "$name$price$timestamp$category"
) : Transaction {
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