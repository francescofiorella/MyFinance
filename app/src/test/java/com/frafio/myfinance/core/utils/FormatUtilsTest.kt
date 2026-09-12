package com.frafio.myfinance.core.utils

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class FormatUtilsTest {

    // activeCurrencyCode is a process-wide mutableStateOf; reset it so test order can't leak.
    @Before
    fun setup() {
        activeCurrencyCode = "EUR"
    }

    @After
    fun teardown() {
        activeCurrencyCode = "EUR"
    }

    @Test
    fun currentLanguage_isPinnedToEnglishByTheTestJvm() {
        // Canary: the enum messages below are frozen at class-init time from Locale.getDefault().
        assertThat(getCurrentLanguage()).isEqualTo("en")
    }

    @Test
    fun doubleToString_alwaysHasTwoDecimals() {
        assertThat(doubleToString(1234.5)).isEqualTo("1234.50")
        assertThat(doubleToString(0.0)).isEqualTo("0.00")
        assertThat(doubleToString(7.0)).isEqualTo("7.00")
    }

    @Test
    fun doubleToString_roundsHalfEven() {
        assertThat(doubleToString(-3.456)).isEqualTo("-3.46")
        // 0.125 and 0.375 are exact in binary, so these are true ties.
        assertThat(doubleToString(0.125)).isEqualTo("0.12")
        assertThat(doubleToString(0.375)).isEqualTo("0.38")
    }

    @Test
    fun doubleToString_hasNoGroupingSeparator() {
        assertThat(doubleToString(1_000_000.0)).isEqualTo("1000000.00")
    }

    @Test
    fun doubleToStringWithoutDecimals_roundsInsteadOfTruncating() {
        assertThat(doubleToStringWithoutDecimals(1234.56)).isEqualTo("1235")
        assertThat(doubleToStringWithoutDecimals(1234.4)).isEqualTo("1234")
    }

    @Test
    fun doubleToStringWithoutDecimals_roundsHalfEven() {
        assertThat(doubleToStringWithoutDecimals(0.5)).isEqualTo("0")
        assertThat(doubleToStringWithoutDecimals(1.5)).isEqualTo("2")
        assertThat(doubleToStringWithoutDecimals(2.5)).isEqualTo("2")
    }

    @Test
    fun doubleToPrice_usesActiveCurrencySymbol() {
        assertThat(doubleToPrice(1234.5)).isEqualTo("€ 1234.50")

        activeCurrencyCode = "USD"
        assertThat(doubleToPrice(1234.5)).isEqualTo("$ 1234.50")

        activeCurrencyCode = "GBP"
        assertThat(doubleToPrice(1234.5)).isEqualTo("£ 1234.50")
    }

    @Test
    fun doubleToPriceWithoutDecimals_usesActiveCurrencySymbol() {
        assertThat(doubleToPriceWithoutDecimals(1234.5)).isEqualTo("€ 1234")

        activeCurrencyCode = "USD"
        assertThat(doubleToPriceWithoutDecimals(99.9)).isEqualTo("$ 100")
    }

    @Test
    fun dateToString_zeroPadsDayAndMonth() {
        assertThat(dateToString(5, 3, 2024)).isEqualTo("05/03/2024")
        assertThat(dateToString(15, 11, 2024)).isEqualTo("15/11/2024")
    }

    @Test
    fun dateToString_isNullWhenAnyPartIsNull() {
        assertThat(dateToString(null, 3, 2024)).isNull()
        assertThat(dateToString(5, null, 2024)).isNull()
        assertThat(dateToString(5, 3, null)).isNull()
    }

    @Test
    fun dateToString_localDateOverloadAgreesWithIntOverload() {
        val date = LocalDate.of(2024, 3, 5)
        assertThat(dateToString(date)).isEqualTo(dateToString(5, 3, 2024))
        assertThat(dateToString(null as LocalDate?)).isNull()
    }

    @Test
    fun dateToExtendedString_usesShortMonthName() {
        assertThat(dateToExtendedString(5, 3, 2024)).isEqualTo("05 Mar 2024")
        assertThat(dateToExtendedString(LocalDate.of(2024, 12, 25))).isEqualTo("25 Dec 2024")
    }

    @Test
    fun dateToExtendedString_isNullWhenAnyPartIsNull() {
        assertThat(dateToExtendedString(null, 3, 2024)).isNull()
        assertThat(dateToExtendedString(5, null, 2024)).isNull()
        assertThat(dateToExtendedString(5, 3, null)).isNull()
        assertThat(dateToExtendedString(null as LocalDate?)).isNull()
    }

    @Test
    fun timeToString_zeroPadsHourAndMinute() {
        assertThat(timeToString(9, 5)).isEqualTo("09:05")
        assertThat(timeToString(23, 59)).isEqualTo("23:59")
        assertThat(timeToString(null, 5)).isNull()
        assertThat(timeToString(9, null)).isNull()
    }

    @Test
    fun dateToUTCTimestamp_isMidnightUtc() {
        assertThat(dateToUTCTimestamp(2024, 1, 1)).isEqualTo(1_704_067_200_000L)
        assertThat(dateToUTCTimestamp(LocalDate.of(2024, 1, 1))).isEqualTo(1_704_067_200_000L)
    }

    @Test
    fun dateToUTCTimestamp_roundTripsThroughToUTCLocalDateTime() {
        val date = LocalDate.of(2023, 7, 14)
        val roundTripped = dateToUTCTimestamp(date).toUTCLocalDateTime()
        assertThat(roundTripped.toLocalDate()).isEqualTo(date)
        assertThat(roundTripped.hour).isEqualTo(0)
        assertThat(roundTripped.minute).isEqualTo(0)
    }

    @Test
    fun currentDeleteAtUTC_isThirtyDaysAhead() {
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        val before = System.currentTimeMillis()
        val deleteAt = currentDeleteAtUTC()
        val after = System.currentTimeMillis()

        assertThat(deleteAt).isAtLeast(before + thirtyDaysMs)
        assertThat(deleteAt).isAtMost(after + thirtyDaysMs)
    }

    @Test
    fun capitalizeWords_titleCasesEachWord() {
        assertThat("mARIO rossi".capitalizeWords()).isEqualTo("Mario Rossi")
        assertThat("single".capitalizeWords()).isEqualTo("Single")
        assertThat("".capitalizeWords()).isEmpty()
    }

    @Test
    fun capitalizeWords_preservesConsecutiveSpaces() {
        assertThat("a  b".capitalizeWords()).isEqualTo("A  B")
    }

    @Test
    fun round_roundsToGivenDecimals() {
        assertThat(3.14159.round(2)).isEqualTo(3.14)
        assertThat(3.14159.round(0)).isEqualTo(3.0)
    }

    @Test
    fun round_tiesGoToEven() {
        assertThat(2.5.round(0)).isEqualTo(2.0)
        assertThat(3.5.round(0)).isEqualTo(4.0)
    }

    @Test
    fun getLocaleFromCurrency_hasHardCodedEuroAndFranc() {
        // "EU" and "FR" are not what the take(2) heuristic would resolve to.
        assertThat(getLocaleFromCurrency("EUR")).isEqualTo(Locale.GERMANY)
        assertThat(getLocaleFromCurrency("FRF")).isEqualTo(Locale.FRANCE)
    }

    @Test
    fun getLocaleFromCurrency_resolvesCountryFromFirstTwoLetters() {
        assertThat(getLocaleFromCurrency("USD").country).isEqualTo("US")
        assertThat(getLocaleFromCurrency("GBP").country).isEqualTo("GB")
        assertThat(getLocaleFromCurrency("JPY").country).isEqualTo("JP")
        assertThat(getLocaleFromCurrency("CHF").country).isEqualTo("CH")
    }

    @Test
    fun getLocaleFromCurrency_fallsBackToUsForUnknownCountry() {
        // XOF (West African franc) has no "XO" country; the heuristic can't resolve it.
        assertThat(getLocaleFromCurrency("XOF")).isEqualTo(Locale.US)
        assertThat(getLocaleFromCurrency("ZZZ")).isEqualTo(Locale.US)
    }

    @Test
    fun getLocaleFromCurrency_toleratesEmptyInput() {
        assertThat(getLocaleFromCurrency("")).isNotNull()
    }
}
