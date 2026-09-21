package com.frafio.myfinance.core.data.dao

import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.testing.data.testIncome
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

internal class IncomeDaoTest : DatabaseTest() {

    private val date = LocalDate.of(2024, 6, 15)

    @Test
    fun getAll_orderIsYearMonthDayPriceDesc() = runTest {
        val newestYear = testIncome(name = "Y", date = LocalDate.of(2025, 1, 1), price = 1.0)
        val newerMonth = testIncome(name = "M", date = LocalDate.of(2024, 7, 1), price = 1.0)
        val newerDay = testIncome(name = "D", date = LocalDate.of(2024, 6, 16), price = 1.0)
        val pricier = testIncome(name = "P", date = date, price = 9.0)
        val baseline = testIncome(name = "B", date = date, price = 1.0)
        incomeDao.insertAll(baseline, pricier, newerDay, newerMonth, newestYear)

        assertThat(incomeDao.getAll().first())
            .containsExactly(newestYear, newerMonth, newerDay, pricier, baseline)
            .inOrder()
    }

    @Test
    fun getCount_tracksInsertsAndDeletes() = runTest {
        assertThat(incomeDao.getCount().first()).isEqualTo(0)

        incomeDao.insertAll(testIncome(name = "A", id = "a"), testIncome(name = "B", id = "b"))
        assertThat(incomeDao.getCount().first()).isEqualTo(2)

        incomeDao.deleteById("a")
        assertThat(incomeDao.getCount().first()).isEqualTo(1)
    }

    @Test
    fun getPriceSumOfYear_noRows_isNull() = runTest {
        assertThat(incomeDao.getPriceSumOfYear(2024).first()).isNull()
    }

    @Test
    fun getPriceSumOfYear_sumsOnlyThatYear() = runTest {
        incomeDao.insertAll(
            testIncome(name = "A", price = 100.0, date = LocalDate.of(2024, 1, 31)),
            testIncome(name = "B", price = 200.0, date = LocalDate.of(2024, 12, 31)),
            testIncome(name = "C", price = 999.0, date = LocalDate.of(2023, 12, 31)),
        )

        assertThat(incomeDao.getPriceSumOfYear(2024).first()).isEqualTo(300.0)
    }

    @Test
    fun getEarliestYearMonth_empty_isNull() = runTest {
        assertThat(incomeDao.getEarliestYearMonth().first()).isNull()
    }

    @Test
    fun getEarliestYearMonth_returnsTheOldestYearThenMonth() = runTest {
        incomeDao.insertAll(
            testIncome(name = "A", date = LocalDate.of(2024, 1, 31)),
            testIncome(name = "B", date = LocalDate.of(2023, 11, 30)),
            testIncome(name = "C", date = LocalDate.of(2023, 12, 31)),
        )

        assertThat(incomeDao.getEarliestYearMonth().first()).isEqualTo(DatePoint(2023, 11))
    }

    @Test
    fun upsert_sameId_replacesTheRow() {
        incomeDao.upsert(testIncome(name = "Old", price = 1.0, id = "same"))

        incomeDao.upsert(testIncome(name = "New", price = 2.0, id = "same"))

        val rows = incomeDao.getAllSync()
        assertThat(rows).hasSize(1)
        assertThat(rows.single().name).isEqualTo("New")
    }

    @Test
    fun updateTable_replacesTheWholeTable() {
        incomeDao.insertAll(testIncome(name = "Old1"), testIncome(name = "Old2"))
        val fresh = testIncome(name = "Fresh")

        incomeDao.updateTable(fresh)

        assertThat(incomeDao.getAllSync()).containsExactly(fresh)
    }

    @Test
    fun insert_defaultIncome_succeedsWithNonNullColumns() = runTest {
        val blank = Income()

        incomeDao.upsert(blank)

        assertThat(incomeDao.getById(blank.id)).isEqualTo(blank)
    }
}
