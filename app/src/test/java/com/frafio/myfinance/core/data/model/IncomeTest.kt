package com.frafio.myfinance.core.data.model

import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.data.testIncome
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.DateTimeException
import java.time.LocalDate

class IncomeTest {

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    @Test
    fun getLocalDate_rebuildsTheDate() {
        val date = LocalDate.of(2023, 11, 30)
        assertThat(testIncome(date = date).getLocalDate()).isEqualTo(date)
    }

    @Test
    fun getLocalDate_throwsWhenAnyPartIsNull() {
        assertThrows(NullPointerException::class.java) { Income(year = null, month = 1, day = 1).getLocalDate() }
        assertThrows(NullPointerException::class.java) { Income(year = 2024, month = null, day = 1).getLocalDate() }
        assertThrows(NullPointerException::class.java) { Income(year = 2024, month = 1, day = null).getLocalDate() }
    }

    @Test
    fun getLocalDate_throwsOnInvalidMonth() {
        assertThrows(DateTimeException::class.java) { Income(year = 2024, month = 13, day = 1).getLocalDate() }
    }

    @Test
    fun getDateString_formatsCompactAndExtended() {
        val income = testIncome(date = LocalDate.of(2024, 1, 31))
        assertThat(income.getDateString(extended = false)).isEqualTo("31/01/2024")
        assertThat(income.getDateString(extended = true)).isEqualTo("31 Jan 2024")
    }

    @Test
    fun getDateString_isEmptyNotNullWhenDateIsMissing() {
        assertThat(Income(name = "x").getDateString()).isEmpty()
    }

    @Test
    fun getPriceString_usesActiveCurrency() {
        assertThat(testIncome(price = 1000.0).getPriceString(showDecimal = true)).isEqualTo("€ 1000.00")
        assertThat(testIncome(price = 1000.6).getPriceString(showDecimal = false)).isEqualTo("€ 1001")
    }

    @Test
    fun defaultId_concatenatesNamePriceTimestampCategoryAndLabels() {
        val income = Income(name = "S", price = 10.0, timestamp = 7L, category = 101)
        assertThat(income.id).isEqualTo("S10.07101[]")
    }

    @Test
    fun copy_keepsTheOriginalId() {
        val original = testIncome(id = "keep-me")
        assertThat(original.copy(price = 1.0).id).isEqualTo("keep-me")
    }
}
