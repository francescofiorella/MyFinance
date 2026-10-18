package com.frafio.myfinance.features.frequentexpenses

import com.frafio.myfinance.core.data.enums.db.FirestoreEnums.CATEGORIES
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class FrequentExpensesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val expensesLocalRepository = TestExpensesLocalRepository()
    private lateinit var viewModel: FrequentExpensesViewModel

    private val dining = CATEGORIES.DINING.value
    private val groceries = CATEGORIES.GROCERIES.value

    @Before
    fun setup() {
        viewModel = FrequentExpensesViewModel(
            expensesLocalRepository,
            UnconfinedTestDispatcher(),
            RootKey.FrequentExpenses(
                categoryId = dining,
                name = "Sushi Bar",
                fromYear = 2023, fromMonth = 6,
                toYear = 2024, toMonth = 5
            )
        )
    }

    @Test
    fun exposesThePeriod_andNoDaysUntilLoaded() {
        assertThat(viewModel.from).isEqualTo(LocalDate.of(2023, 6, 1))
        assertThat(viewModel.to).isEqualTo(LocalDate.of(2024, 5, 1))
        assertThat(viewModel.days.value).isNull()
    }

    @Test
    fun days_keepTheCategoryPeriodAndName_ignoringCaseAndSpaces() = runTest {
        collectDays()
        val first = testExpense(name = "Sushi Bar", price = 30.0, date = LocalDate.of(2024, 5, 31), category = dining, id = "a")
        val sameName = testExpense(name = " sushi bar ", price = 20.0, date = LocalDate.of(2023, 6, 1), category = dining, id = "b")

        expensesLocalRepository.sendExpenses(
            first,
            sameName,
            testExpense(name = "Sushi Bar", date = LocalDate.of(2023, 5, 31), category = dining, id = "before"),
            testExpense(name = "Sushi Bar", date = LocalDate.of(2024, 6, 1), category = dining, id = "after"),
            testExpense(name = "Sushi Bar", date = LocalDate.of(2024, 5, 10), category = groceries, id = "otherCategory"),
            testExpense(name = "Pizza", date = LocalDate.of(2024, 5, 10), category = dining, id = "otherName"),
        )

        val expenses = viewModel.days.value!!.flatMap { it.expenses }
        assertThat(expenses).containsExactly(first, sameName).inOrder()
    }

    @Test
    fun days_groupTheExpensesByDay_newestFirst() = runTest {
        collectDays()
        val may10 = LocalDate.of(2024, 5, 10)
        val april2 = LocalDate.of(2024, 4, 2)

        expensesLocalRepository.sendExpenses(
            testExpense(name = "Sushi Bar", price = 10.0, date = april2, category = dining, id = "a"),
            testExpense(name = "Sushi Bar", price = 20.0, date = may10, category = dining, id = "b"),
            testExpense(name = "Sushi Bar", price = 30.0, date = may10, category = dining, id = "c"),
        )

        val days = viewModel.days.value!!
        assertThat(days.map { it.date }).containsExactly(may10, april2).inOrder()
        assertThat(days.first().expenses.map { it.id }).containsExactly("c", "b").inOrder()
    }

    private fun TestScope.collectDays() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.days.collect() }
    }
}
