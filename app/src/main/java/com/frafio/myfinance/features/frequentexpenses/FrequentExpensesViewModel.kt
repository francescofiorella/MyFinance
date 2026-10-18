package com.frafio.myfinance.features.frequentexpenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers.Default
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.utils.expenseNameKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** One day of the list, with its expenses in the order the DAO returns them. */
data class ExpensesOfDay(
    val date: LocalDate,
    val expenses: List<Expense>
)

/** Groups expenses already sorted newest first by day, keeping that order. */
internal fun groupByDay(expenses: List<Expense>): List<ExpensesOfDay> =
    expenses.groupBy { it.getLocalDate() }.map { (date, ofDay) -> ExpensesOfDay(date, ofDay) }

@HiltViewModel(assistedFactory = FrequentExpensesViewModel.Factory::class)
class FrequentExpensesViewModel @AssistedInject constructor(
    expensesLocalRepository: ExpensesLocalRepository,
    @Dispatcher(Default) defaultDispatcher: CoroutineDispatcher,
    @Assisted navKey: RootKey.FrequentExpenses
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: RootKey.FrequentExpenses): FrequentExpensesViewModel
    }

    /** First and last month covered, both included. */
    val from: LocalDate = LocalDate.of(navKey.fromYear, navKey.fromMonth, 1)
    val to: LocalDate = LocalDate.of(navKey.toYear, navKey.toMonth, 1)

    /** Null until loaded. */
    val days: StateFlow<List<ExpensesOfDay>?> = run {
        val end = to.plusMonths(1)
        val nameKey = expenseNameKey(navKey.name)
        expensesLocalRepository.getExpensesOfCategory(
            navKey.categoryId,
            from.year, from.monthValue,
            end.year, end.monthValue
        )
            .map { expenses -> groupByDay(expenses.filter { expenseNameKey(it.name) == nameKey }) }
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
}
