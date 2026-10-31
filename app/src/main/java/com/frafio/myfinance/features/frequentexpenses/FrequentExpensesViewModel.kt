package com.frafio.myfinance.features.frequentexpenses

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers.Default
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.utils.expenseNameKey
import com.frafio.myfinance.core.utils.monthRangeToString
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
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

    /** The category within a label, otherwise the frequent expenses title. */
    @StringRes
    val titleRes: Int

    /** Under the title: the months covered, or the label. */
    val subtitle: String

    /** Null until loaded. */
    val days: StateFlow<List<ExpensesOfDay>?>

    init {
        val expenses = when (val scope = navKey.scope) {
            is RootKey.FrequentExpenses.Scope.NameInCategory -> {
                val from = LocalDate.of(scope.fromYear, scope.fromMonth, 1)
                val to = LocalDate.of(scope.toYear, scope.toMonth, 1)
                val end = to.plusMonths(1)
                val nameKey = expenseNameKey(scope.name)
                titleRes = R.string.frequent_expenses_title
                subtitle = monthRangeToString(from, to)
                expensesLocalRepository.getExpensesOfCategory(
                    scope.categoryId,
                    from.year, from.monthValue,
                    end.year, end.monthValue
                ).map { ofCategory -> ofCategory.filter { expenseNameKey(it.name) == nameKey } }
            }

            is RootKey.FrequentExpenses.Scope.NameInLabel -> {
                val nameKey = expenseNameKey(scope.name)
                titleRes = R.string.frequent_expenses_title
                subtitle = scope.label
                expensesWithLabel(expensesLocalRepository, scope.label) { expenseNameKey(it.name) == nameKey }
            }

            is RootKey.FrequentExpenses.Scope.CategoryInLabel -> {
                titleRes = ExpenseCategories.get(scope.categoryId).nameRes
                subtitle = scope.label
                expensesWithLabel(expensesLocalRepository, scope.label) { it.category == scope.categoryId }
            }
        }
        days = expenses
            .map(::groupByDay)
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
}

/** The expenses with [label] matching [predicate], keeping only their other labels: this one is on every row. */
private fun expensesWithLabel(
    expensesLocalRepository: ExpensesLocalRepository,
    label: String,
    predicate: (Expense) -> Boolean
): Flow<List<Expense>> =
    expensesLocalRepository.getLabelled().map { labelled ->
        labelled.filter { label in it.labels && predicate(it) }.map { it.copy(labels = it.labels - label) }
    }
