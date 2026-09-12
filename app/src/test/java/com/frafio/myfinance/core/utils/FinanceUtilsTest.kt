package com.frafio.myfinance.core.utils

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.data.testIncome
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class FinanceUtilsTest {

    private val today: LocalDate = LocalDate.of(2024, 6, 15)
    private val yesterday: LocalDate = today.minusDays(1)
    private val tomorrow: LocalDate = today.plusDays(1)

    private val totalCategory = FirestoreEnums.CATEGORIES.TOTAL.value
    private val jollyCategory = FirestoreEnums.CATEGORIES.JOLLY.value
    private val totalName = FirestoreEnums.NAMES.TOTAL.value

    // region addTotalsToExpenses

    @Test
    fun addTotalsToExpenses_emptyInput_isEmpty() {
        assertThat(addTotalsToExpenses(emptyList(), today)).isEmpty()
    }

    @Test
    fun addTotalsToExpenses_groupOnToday_prependsTotalWithoutJolly() {
        val a = testExpense(name = "A", price = 2.0, date = today)
        val b = testExpense(name = "B", price = 3.5, date = today)

        val result = addTotalsToExpenses(listOf(a, b), today)

        assertThat(result).hasSize(3)
        assertThat(result[0].isTotalFor(today, 5.5)).isTrue()
        assertThat(result.drop(1)).containsExactly(a, b).inOrder()
        assertThat(result.none { it.category == jollyCategory }).isTrue()
    }

    @Test
    fun addTotalsToExpenses_groupBeforeToday_insertsEmptyTodayBlockFirst() {
        val old = testExpense(name = "Old", price = 4.0, date = yesterday)

        val result = addTotalsToExpenses(listOf(old), today)

        assertThat(result).hasSize(4)
        assertThat(result[0].isTotalFor(today, 0.0)).isTrue()
        assertThat(result[1].isJollyFor(today)).isTrue()
        assertThat(result[2].isTotalFor(yesterday, 4.0)).isTrue()
        assertThat(result[3]).isEqualTo(old)
    }

    @Test
    fun addTotalsToExpenses_allFuture_appendsTodayBlockLast() {
        val future = testExpense(name = "Future", price = 1.0, date = tomorrow)

        val result = addTotalsToExpenses(listOf(future), today)

        assertThat(result).hasSize(4)
        assertThat(result[0].isTotalFor(tomorrow, 1.0)).isTrue()
        assertThat(result[1]).isEqualTo(future)
        assertThat(result[2].isTotalFor(today, 0.0)).isTrue()
        assertThat(result[3].isJollyFor(today)).isTrue()
    }

    @Test
    fun addTotalsToExpenses_futureAndPast_insertsTodayBlockBetweenThemOnce() {
        val future = testExpense(name = "Future", price = 1.0, date = tomorrow)
        val past = testExpense(name = "Past", price = 2.0, date = yesterday)

        val result = addTotalsToExpenses(listOf(future, past), today)

        assertThat(result.map { it.id }).containsExactly(
            "total_${tomorrow.id()}", future.id,
            "total_${today.id()}", "jolly_${today.id()}",
            "total_${yesterday.id()}", past.id,
        ).inOrder()
        assertThat(result.count { it.category == jollyCategory }).isEqualTo(1)
    }

    @Test
    fun addTotalsToExpenses_sumsEachDayGroupSeparately() {
        val a = testExpense(name = "A", price = 1.0, date = today)
        val b = testExpense(name = "B", price = 2.0, date = today)
        val c = testExpense(name = "C", price = 10.0, date = yesterday)

        val result = addTotalsToExpenses(listOf(a, b, c), today)

        assertThat(result[0].isTotalFor(today, 3.0)).isTrue()
        assertThat(result[3].isTotalFor(yesterday, 10.0)).isTrue()
    }

    @Test
    fun addTotalsToExpenses_nullPriceCountsAsZero() {
        val priced = testExpense(name = "Priced", price = 5.0, date = today)
        val unpriced = testExpense(name = "Unpriced", price = null, date = today)

        val result = addTotalsToExpenses(listOf(priced, unpriced), today)

        assertThat(result[0].price).isEqualTo(5.0)
    }

    @Test
    fun addTotalsToExpenses_totalAndJollyIdsUseUnpaddedDayMonthYear() {
        val single = testExpense(date = LocalDate.of(2024, 3, 5))
        val todayIs = LocalDate.of(2024, 3, 9)

        val result = addTotalsToExpenses(listOf(single), todayIs)

        assertThat(result[0].id).isEqualTo("total_9_3_2024")
        assertThat(result[1].id).isEqualTo("jolly_9_3_2024")
        assertThat(result[2].id).isEqualTo("total_5_3_2024")
    }

    @Test
    fun addTotalsToExpenses_preservesOrderInsideAGroup() {
        val expenses = (1..5).map { testExpense(name = "E$it", price = it.toDouble(), date = today) }

        val result = addTotalsToExpenses(expenses, today)

        assertThat(result.drop(1)).containsExactlyElementsIn(expenses).inOrder()
    }

    @Test
    fun addTotalsToExpenses_groupsPositionallyNotByValue() {
        // Characterization: the same date appearing non-contiguously yields two TOTAL rows.
        val a = testExpense(name = "A", price = 1.0, date = today)
        val b = testExpense(name = "B", price = 1.0, date = yesterday)
        val c = testExpense(name = "C", price = 1.0, date = today)

        val result = addTotalsToExpenses(listOf(a, b, c), today)

        assertThat(result.filter { it.category == totalCategory }.map { it.id })
            .containsExactly("total_${today.id()}", "total_${yesterday.id()}", "total_${today.id()}")
            .inOrder()
    }

    @Test
    fun addTotalsToExpenses_defaultsTodayToNow() {
        val onNow = testExpense(date = LocalDate.now())

        val result = addTotalsToExpenses(listOf(onNow))

        assertThat(result[0].isTotalFor(LocalDate.now(), onNow.price!!)).isTrue()
        assertThat(result.none { it.category == jollyCategory }).isTrue()
    }

    // endregion

    // region addTotalsToExpensesWithoutToday

    @Test
    fun addTotalsToExpensesWithoutToday_emptyInput_isEmpty() {
        assertThat(addTotalsToExpensesWithoutToday(emptyList())).isEmpty()
    }

    @Test
    fun addTotalsToExpensesWithoutToday_neverInsertsATodayBlock() {
        val old = testExpense(name = "Old", price = 4.0, date = LocalDate.of(2020, 1, 1))

        val result = addTotalsToExpensesWithoutToday(listOf(old))

        assertThat(result).hasSize(2)
        assertThat(result[0].isTotalFor(LocalDate.of(2020, 1, 1), 4.0)).isTrue()
        assertThat(result[1]).isEqualTo(old)
        assertThat(result.none { it.category == jollyCategory }).isTrue()
    }

    @Test
    fun addTotalsToExpensesWithoutToday_sumsAndOrdersEachGroup() {
        val a = testExpense(name = "A", price = 1.0, date = today)
        val b = testExpense(name = "B", price = 2.0, date = today)
        val c = testExpense(name = "C", price = 10.0, date = yesterday)

        val result = addTotalsToExpensesWithoutToday(listOf(a, b, c))

        assertThat(result.map { it.id }).containsExactly(
            "total_${today.id()}", a.id, b.id,
            "total_${yesterday.id()}", c.id,
        ).inOrder()
        assertThat(result[0].price).isEqualTo(3.0)
        assertThat(result[3].price).isEqualTo(10.0)
    }

    @Test
    fun addTotalsToExpensesWithoutToday_nullPriceThrows() {
        val unpriced = testExpense(price = null)

        assertThrows(NullPointerException::class.java) {
            addTotalsToExpensesWithoutToday(listOf(unpriced))
        }
    }

    // endregion

    // region addTotalsToIncomes

    @Test
    fun addTotalsToIncomes_emptyInput_isEmpty() {
        assertThat(addTotalsToIncomes(emptyList(), today)).isEmpty()
    }

    @Test
    fun addTotalsToIncomes_allInCurrentYear_prependsSingleYearTotal() {
        val jan = testIncome(name = "Jan", price = 100.0, date = LocalDate.of(2024, 1, 31))
        val feb = testIncome(name = "Feb", price = 200.0, date = LocalDate.of(2024, 2, 29))

        val result = addTotalsToIncomes(listOf(jan, feb), today)

        assertThat(result).hasSize(3)
        assertThat(result[0].isYearTotal(2024, 300.0)).isTrue()
        assertThat(result.drop(1)).containsExactly(jan, feb).inOrder()
    }

    @Test
    fun addTotalsToIncomes_firstIncomeInPastYear_insertsEmptyCurrentYearBlock() {
        val old = testIncome(name = "Old", price = 50.0, date = LocalDate.of(2022, 5, 1))

        val result = addTotalsToIncomes(listOf(old), today)

        assertThat(result).hasSize(4)
        assertThat(result[0].isYearTotal(2024, 0.0)).isTrue()
        assertThat(result[1].category).isEqualTo(jollyCategory)
        assertThat(result[1].name).isEmpty()
        assertThat(result[1].year).isEqualTo(today.year)
        assertThat(result[1].month).isEqualTo(today.monthValue)
        assertThat(result[1].day).isEqualTo(today.dayOfMonth)
        assertThat(result[2].isYearTotal(2022, 50.0)).isTrue()
        assertThat(result[3]).isEqualTo(old)
    }

    @Test
    fun addTotalsToIncomes_currentYearTotalAndJollyShareAnId() {
        // Characterization: both placeholder rows carry id == today.year, a duplicate-key risk
        // for any keyed list rendering them.
        val old = testIncome(date = LocalDate.of(2022, 5, 1))

        val result = addTotalsToIncomes(listOf(old), today)

        assertThat(result[0].id).isEqualTo("2024")
        assertThat(result[1].id).isEqualTo("2024")
    }

    @Test
    fun addTotalsToIncomes_firstIncomeInFutureYear_hasNoCurrentYearBlock() {
        val future = testIncome(name = "Future", price = 70.0, date = LocalDate.of(2025, 1, 1))

        val result = addTotalsToIncomes(listOf(future), today)

        assertThat(result).hasSize(2)
        assertThat(result[0].isYearTotal(2025, 70.0)).isTrue()
        assertThat(result[1]).isEqualTo(future)
    }

    @Test
    fun addTotalsToIncomes_splitsAtYearBoundary() {
        val a24 = testIncome(name = "A", price = 1.0, date = LocalDate.of(2024, 3, 1))
        val b24 = testIncome(name = "B", price = 2.0, date = LocalDate.of(2024, 1, 1))
        val c23 = testIncome(name = "C", price = 10.0, date = LocalDate.of(2023, 12, 1))

        val result = addTotalsToIncomes(listOf(a24, b24, c23), today)

        assertThat(result.map { it.id }).containsExactly(
            "2024", a24.id, b24.id,
            "2023", c23.id,
        ).inOrder()
        assertThat(result[0].price).isEqualTo(3.0)
        assertThat(result[3].price).isEqualTo(10.0)
    }

    @Test
    fun addTotalsToIncomes_yearTotalsHaveZeroMonthAndDay() {
        val income = testIncome(date = LocalDate.of(2024, 3, 1))

        val result = addTotalsToIncomes(listOf(income), today)

        assertThat(result[0].month).isEqualTo(0)
        assertThat(result[0].day).isEqualTo(0)
    }

    @Test
    fun addTotalsToIncomes_nullYearThrows() {
        val noYear = Income(name = "X", price = 1.0, year = null, month = 1, day = 1)

        assertThrows(NullPointerException::class.java) {
            addTotalsToIncomes(listOf(noYear), today)
        }
    }

    @Test
    fun addTotalsToIncomes_nullPriceThrows() {
        val unpriced = testIncome(price = null, date = LocalDate.of(2024, 3, 1))

        assertThrows(NullPointerException::class.java) {
            addTotalsToIncomes(listOf(unpriced), today)
        }
    }

    // endregion

    private fun LocalDate.id() = "${dayOfMonth}_${monthValue}_$year"

    private fun Expense.isTotalFor(date: LocalDate, sum: Double): Boolean =
        name == totalName &&
            category == totalCategory &&
            id == "total_${date.id()}" &&
            year == date.year && month == date.monthValue && day == date.dayOfMonth &&
            price == sum

    private fun Expense.isJollyFor(date: LocalDate): Boolean =
        name == "" &&
            category == jollyCategory &&
            id == "jolly_${date.id()}" &&
            year == date.year && month == date.monthValue && day == date.dayOfMonth &&
            price == 0.0

    private fun Income.isYearTotal(totalYear: Int, sum: Double): Boolean =
        name == totalName &&
            category == totalCategory &&
            id == totalYear.toString() &&
            year == totalYear &&
            price == sum
}
