package com.frafio.myfinance.core.data.dao

import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
import com.frafio.myfinance.testing.data.testExpense
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

internal class ExpenseDaoTest : DatabaseTest() {

    private val spending = (0..8).toList()
    private val date = LocalDate.of(2024, 6, 15)

    // region writes

    @Test
    fun insertAll_thenGetAllSync_returnsEveryRow() {
        val a = testExpense(name = "A", date = date)
        val b = testExpense(name = "B", date = date)

        expenseDao.insertAll(a, b)

        assertThat(expenseDao.getAllSync()).containsExactly(a, b)
    }

    @Test
    fun upsert_sameId_replacesTheRow() {
        expenseDao.upsert(testExpense(name = "Old", price = 1.0, id = "same"))

        expenseDao.upsert(testExpense(name = "New", price = 2.0, id = "same"))

        val rows = expenseDao.getAllSync()
        assertThat(rows).hasSize(1)
        assertThat(rows.single().name).isEqualTo("New")
        assertThat(rows.single().price).isEqualTo(2.0)
    }

    @Test
    fun deleteById_removesOnlyThatRow() {
        val keep = testExpense(name = "Keep", id = "keep")
        expenseDao.insertAll(keep, testExpense(name = "Drop", id = "drop"))

        expenseDao.deleteById("drop")

        assertThat(expenseDao.getAllSync()).containsExactly(keep)
    }

    @Test
    fun deleteAll_emptiesTheTable() {
        expenseDao.insertAll(testExpense(name = "A"), testExpense(name = "B"))

        expenseDao.deleteAll()

        assertThat(expenseDao.getAllSync()).isEmpty()
    }

    @Test
    fun updateTable_replacesTheWholeTable() {
        expenseDao.insertAll(testExpense(name = "Old1"), testExpense(name = "Old2"))
        val fresh = testExpense(name = "Fresh")

        expenseDao.updateTable(fresh)

        assertThat(expenseDao.getAllSync()).containsExactly(fresh)
    }

    @Test
    fun getById_missing_returnsNull() = runTest {
        assertThat(expenseDao.getById("nope")).isNull()
    }

    @Test
    fun getById_present_returnsTheRow() = runTest {
        val row = testExpense(name = "Row", id = "row")
        expenseDao.upsert(row)

        assertThat(expenseDao.getById("row")).isEqualTo(row)
    }

    @Test
    fun insert_defaultExpense_succeedsWithNonNullColumns() = runTest {
        val blank = Expense()

        expenseDao.upsert(blank)

        assertThat(expenseDao.getById(blank.id)).isEqualTo(blank)
    }

    @Test
    fun labels_roundTripThroughTheConverter() = runTest {
        val tagged = testExpense(name = "Tagged", labels = listOf("work", "café"), id = "tagged")

        expenseDao.upsert(tagged)

        assertThat(expenseDao.getById("tagged")?.labels).containsExactly("work", "café").inOrder()
    }

    // endregion

    // region getWithFilter

    @Test
    fun getWithFilter_emptyName_matchesEverythingInTheCategories() = runTest {
        val dining = testExpense(name = "Coffee", category = 5)
        val housing = testExpense(name = "Rent", category = 0)
        val income = testExpense(name = "Salary", category = 101)
        expenseDao.insertAll(dining, housing, income)

        val rows = expenseDao.getWithFilter("", spending).first()

        assertThat(rows).containsExactly(dining, housing)
    }

    @Test
    fun getWithFilter_prefix_matchesFromTheStartNotMidWord() = runTest {
        val coffee = testExpense(name = "Coffee")
        val midWord = testExpense(name = "Icedcoffee")
        val decaf = testExpense(name = "Decaf")
        expenseDao.insertAll(coffee, midWord, decaf)

        val rows = expenseDao.getWithFilter("Cof", spending).first()

        assertThat(rows).containsExactly(coffee)
    }

    @Test
    fun getWithFilter_wordStart_matchesAfterASpaceOnly() = runTest {
        val iced = testExpense(name = "Iced coffee")
        val ebike = testExpense(name = "e-bike")
        expenseDao.insertAll(iced, ebike)

        assertThat(expenseDao.getWithFilter("cof", spending).first()).containsExactly(iced)
        assertThat(expenseDao.getWithFilter("bike", spending).first()).isEmpty()
    }

    @Test
    fun getWithFilter_isCaseInsensitiveForAscii() = runTest {
        val coffee = testExpense(name = "Coffee")
        expenseDao.upsert(coffee)

        assertThat(expenseDao.getWithFilter("coffee", spending).first()).containsExactly(coffee)
        assertThat(expenseDao.getWithFilter("COFFEE", spending).first()).containsExactly(coffee)
    }

    @Test
    fun getWithFilter_escapedPercent_isLiteral() = runTest {
        val discount = testExpense(name = "50% off")
        val fiveHundred = testExpense(name = "500")
        expenseDao.insertAll(discount, fiveHundred)

        // ExpensesLocalRepositoryImpl sends the term already escaped.
        assertThat(expenseDao.getWithFilter("50\\%", spending).first()).containsExactly(discount)
    }

    @Test
    fun getWithFilter_escapedUnderscore_isLiteral() = runTest {
        val underscore = testExpense(name = "a_b")
        val axb = testExpense(name = "axb")
        expenseDao.insertAll(underscore, axb)

        assertThat(expenseDao.getWithFilter("a\\_b", spending).first()).containsExactly(underscore)
    }

    @Test
    fun getWithFilter_excludesOtherCategories() = runTest {
        val dining = testExpense(name = "Coffee", category = 5)
        val housing = testExpense(name = "Coffee table", category = 0)
        expenseDao.insertAll(dining, housing)

        assertThat(expenseDao.getWithFilter("Coffee", listOf(5)).first()).containsExactly(dining)
    }

    @Test
    fun getWithFilter_orderIsYearMonthDayPriceCategoryDesc() = runTest {
        val newestYear = testExpense(name = "Y", date = LocalDate.of(2025, 1, 1), price = 1.0, category = 0)
        val newerMonth = testExpense(name = "M", date = LocalDate.of(2024, 7, 1), price = 1.0, category = 0)
        val newerDay = testExpense(name = "D", date = LocalDate.of(2024, 6, 16), price = 1.0, category = 0)
        val pricier = testExpense(name = "P", date = date, price = 9.0, category = 0)
        val higherCategory = testExpense(name = "C", date = date, price = 1.0, category = 8)
        val baseline = testExpense(name = "B", date = date, price = 1.0, category = 0)
        expenseDao.insertAll(baseline, higherCategory, pricier, newerDay, newerMonth, newestYear)

        val rows = expenseDao.getWithFilter("", spending).first()

        assertThat(rows)
            .containsExactly(newestYear, newerMonth, newerDay, pricier, higherCategory, baseline)
            .inOrder()
    }

    @Test
    fun getWithFilterDate_startIsInclusiveEndIsExclusive() = runTest {
        val first = LocalDate.of(2024, 3, 1)
        val last = LocalDate.of(2024, 3, 31)
        val onFirst = testExpense(name = "First", date = first)
        val onLast = testExpense(name = "Last", date = last)
        val dayAfter = testExpense(name = "After", date = last.plusDays(1))
        val dayBefore = testExpense(name = "Before", date = first.minusDays(1))
        expenseDao.insertAll(onFirst, onLast, dayAfter, dayBefore)

        val rows = expenseDao.getWithFilterDate(
            "", spending, dateToUTCTimestamp(first), dateToUTCTimestamp(last.plusDays(1)),
        ).first()

        assertThat(rows).containsExactly(onLast, onFirst).inOrder()
    }

    @Test
    fun getWithFilterDate_appliesNameAndCategoryToo() = runTest {
        val match = testExpense(name = "Coffee", category = 5, date = date)
        val wrongName = testExpense(name = "Tea", category = 5, date = date)
        val wrongCategory = testExpense(name = "Coffee maker", category = 0, date = date)
        expenseDao.insertAll(match, wrongName, wrongCategory)

        val rows = expenseDao.getWithFilterDate(
            "Cof", listOf(5), dateToUTCTimestamp(date), dateToUTCTimestamp(date.plusDays(1)),
        ).first()

        assertThat(rows).containsExactly(match)
    }

    // endregion

    // region aggregates

    @Test
    fun getCount_tracksInsertsAndDeletes() = runTest {
        assertThat(expenseDao.getCount().first()).isEqualTo(0)

        expenseDao.insertAll(testExpense(name = "A", id = "a"), testExpense(name = "B", id = "b"))
        assertThat(expenseDao.getCount().first()).isEqualTo(2)

        expenseDao.deleteById("a")
        assertThat(expenseDao.getCount().first()).isEqualTo(1)
    }

    @Test
    fun getPriceSumOfDay_noRows_isNull() = runTest {
        assertThat(expenseDao.getPriceSumOfDay(2024, 6, 15).first()).isNull()
    }

    @Test
    fun getPriceSumOfDay_sumsOnlyThatDay() = runTest {
        expenseDao.insertAll(
            testExpense(name = "A", price = 1.5, date = date),
            testExpense(name = "B", price = 2.0, date = date),
            testExpense(name = "C", price = 100.0, date = date.plusDays(1)),
        )

        assertThat(expenseDao.getPriceSumOfDay(2024, 6, 15).first()).isEqualTo(3.5)
    }

    @Test
    fun getPriceSumOfMonth_noRows_isNull() = runTest {
        assertThat(expenseDao.getPriceSumOfMonth(2024, 6).first()).isNull()
    }

    @Test
    fun getPriceSumOfMonth_sumsOnlyThatMonth() = runTest {
        expenseDao.insertAll(
            testExpense(name = "A", price = 1.0, date = LocalDate.of(2024, 6, 1)),
            testExpense(name = "B", price = 2.0, date = LocalDate.of(2024, 6, 30)),
            testExpense(name = "C", price = 100.0, date = LocalDate.of(2024, 7, 1)),
            testExpense(name = "D", price = 100.0, date = LocalDate.of(2023, 6, 15)),
        )

        assertThat(expenseDao.getPriceSumOfMonth(2024, 6).first()).isEqualTo(3.0)
    }

    @Test
    fun getPriceSumOfYear_noRows_isNull() = runTest {
        assertThat(expenseDao.getPriceSumOfYear(2024).first()).isNull()
    }

    @Test
    fun getPriceSumOfYear_sumsOnlyThatYear() = runTest {
        expenseDao.insertAll(
            testExpense(name = "A", price = 1.0, date = LocalDate.of(2024, 1, 1)),
            testExpense(name = "B", price = 2.0, date = LocalDate.of(2024, 12, 31)),
            testExpense(name = "C", price = 100.0, date = LocalDate.of(2025, 1, 1)),
        )

        assertThat(expenseDao.getPriceSumOfYear(2024).first()).isEqualTo(3.0)
    }

    @Test
    fun getPriceSumAfterAndBefore_groupsByMonthNewestFirst() = runTest {
        expenseDao.insertAll(
            testExpense(name = "Apr1", price = 1.0, date = LocalDate.of(2024, 4, 1)),
            testExpense(name = "Apr2", price = 2.0, date = LocalDate.of(2024, 4, 20)),
            testExpense(name = "May", price = 10.0, date = LocalDate.of(2024, 5, 5)),
            testExpense(name = "Jun", price = 100.0, date = LocalDate.of(2024, 6, 5)),
        )

        val entries = expenseDao.getPriceSumAfterAndBefore(2024, 4, 2024, 7).first()

        assertThat(entries).containsExactly(
            BarChartEntry(100.0, 2024, 6),
            BarChartEntry(10.0, 2024, 5),
            BarChartEntry(3.0, 2024, 4),
        ).inOrder()
    }

    @Test
    fun getPriceSumAfterAndBefore_crossesTheYearBoundary() = runTest {
        expenseDao.insertAll(
            testExpense(name = "Oct", price = 1.0, date = LocalDate.of(2023, 10, 1)),
            testExpense(name = "Nov", price = 2.0, date = LocalDate.of(2023, 11, 1)),
            testExpense(name = "Dec", price = 4.0, date = LocalDate.of(2023, 12, 1)),
            testExpense(name = "Jan", price = 8.0, date = LocalDate.of(2024, 1, 1)),
            testExpense(name = "Feb", price = 16.0, date = LocalDate.of(2024, 2, 1)),
        )

        // [2023-11, 2024-02): November, December and January.
        val entries = expenseDao.getPriceSumAfterAndBefore(2023, 11, 2024, 2).first()

        assertThat(entries).containsExactly(
            BarChartEntry(8.0, 2024, 1),
            BarChartEntry(4.0, 2023, 12),
            BarChartEntry(2.0, 2023, 11),
        ).inOrder()
    }

    @Test
    fun getPriceSumAfterAndBefore_omitsEmptyMonths() = runTest {
        expenseDao.insertAll(
            testExpense(name = "Jan", price = 1.0, date = LocalDate.of(2024, 1, 1)),
            testExpense(name = "Mar", price = 3.0, date = LocalDate.of(2024, 3, 1)),
        )

        val entries = expenseDao.getPriceSumAfterAndBefore(2024, 1, 2024, 4).first()

        assertThat(entries.map { it.month }).containsExactly(3, 1).inOrder()
    }

    @Test
    fun getEarliestYearMonth_empty_isNull() = runTest {
        assertThat(expenseDao.getEarliestYearMonth().first()).isNull()
    }

    @Test
    fun getEarliestYearMonth_returnsTheOldestYearThenMonth() = runTest {
        expenseDao.insertAll(
            testExpense(name = "A", date = LocalDate.of(2024, 1, 5)),
            testExpense(name = "B", date = LocalDate.of(2023, 12, 25)),
            testExpense(name = "C", date = LocalDate.of(2023, 12, 1)),
        )

        assertThat(expenseDao.getEarliestYearMonth().first()).isEqualTo(DatePoint(2023, 12))
    }

    @Test
    fun getExpensesOfMonth_filtersExactly() = runTest {
        val june = testExpense(name = "June", date = LocalDate.of(2024, 6, 15))
        expenseDao.insertAll(
            june,
            testExpense(name = "July", date = LocalDate.of(2024, 7, 15)),
            testExpense(name = "LastJune", date = LocalDate.of(2023, 6, 15)),
        )

        assertThat(expenseDao.getExpensesOfMonth(2024, 6).first()).containsExactly(june)
    }

    @Test
    fun getExpensesOfYear_filtersExactly() = runTest {
        val a = testExpense(name = "A", date = LocalDate.of(2024, 1, 1))
        val b = testExpense(name = "B", date = LocalDate.of(2024, 12, 31))
        expenseDao.insertAll(a, b, testExpense(name = "C", date = LocalDate.of(2025, 1, 1)))

        assertThat(expenseDao.getExpensesOfYear(2024).first()).containsExactly(a, b)
    }

    // endregion
}
