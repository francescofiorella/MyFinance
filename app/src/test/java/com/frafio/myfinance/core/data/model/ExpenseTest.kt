package com.frafio.myfinance.core.data.model

import com.frafio.myfinance.core.utils.activeCurrencyCode
import com.frafio.myfinance.testing.data.testExpense
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.DateTimeException
import java.time.LocalDate

class ExpenseTest {

    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    @Test
    fun getTotalId_isUnpaddedDayMonthYear() {
        assertThat(testExpense(date = LocalDate.of(2024, 1, 5)).getTotalId()).isEqualTo("5_1_2024")
    }

    @Test
    fun getLocalDate_rebuildsTheDate() {
        val date = LocalDate.of(2023, 11, 30)
        assertThat(testExpense(date = date).getLocalDate()).isEqualTo(date)
    }

    @Test
    fun getLocalDate_throwsWhenTheDateIsUnset() {
        // The defaults are 0/0/0, which is not a date; an unset row must not pass silently.
        assertThrows(DateTimeException::class.java) { Expense(name = "x").getLocalDate() }
    }

    @Test
    fun getLocalDate_throwsOnInvalidMonth() {
        assertThrows(DateTimeException::class.java) { Expense(year = 2024, month = 13, day = 1).getLocalDate() }
    }

    @Test
    fun getDateString_formatsCompactAndExtended() {
        val expense = testExpense(date = LocalDate.of(2024, 1, 15))
        assertThat(expense.getDateString(extended = false)).isEqualTo("15/01/2024")
        assertThat(expense.getDateString(extended = true)).isEqualTo("15 Jan 2024")
    }

    @Test
    fun getPriceString_usesActiveCurrency() {
        assertThat(testExpense(price = 1.5).getPriceString(showDecimal = true)).isEqualTo("€ 1.50")
        assertThat(testExpense(price = 2.4).getPriceString(showDecimal = false)).isEqualTo("€ 2")
    }

    @Test
    fun defaultId_concatenatesNamePriceTimestampCategoryAndLabels() {
        // This is the Room primary key; changing the format silently duplicates rows on sync.
        val expense = Expense(name = "A", price = 1.0, timestamp = 5L, category = 2, labels = listOf("x"))
        assertThat(expense.id).isEqualTo("A1.052[x]")
    }

    @Test
    fun copy_keepsTheOriginalId() {
        val original = testExpense(id = "keep-me")
        assertThat(original.copy(category = 7).id).isEqualTo("keep-me")
    }
}
