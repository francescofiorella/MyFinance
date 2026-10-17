package com.frafio.myfinance.features.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers.Default
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

data class CategoriesUiState(
    /** The first day of the selected month. */
    val month: LocalDate,
    /** Empty until the month's data has loaded. */
    val spending: List<CategorySpending> = emptyList(),
    val isPreviousEnabled: Boolean = false,
    val isNextEnabled: Boolean = false
) {
    val total: Double get() = spending.sumOf { it.total }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val expensesLocalRepository: ExpensesLocalRepository,
    @Dispatcher(Default) private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val currentMonth: LocalDate = LocalDate.now().withDayOfMonth(1)

    private val selectedMonth = MutableStateFlow(currentMonth)

    private val earliestMonth = expensesLocalRepository.getEarliestYearMonth()
        .map { it?.let { point -> LocalDate.of(point.year, point.month, 1) } }

    private val spending = selectedMonth.flatMapLatest { month ->
        val previous = month.minusMonths(1)
        combine(
            expensesLocalRepository.getCategorySumsOfMonth(month.year, month.monthValue),
            expensesLocalRepository.getCategorySumsOfMonth(previous.year, previous.monthValue)
        ) { current, before ->
            month to buildCategorySpending(current, before)
        }
    }

    val uiState: StateFlow<CategoriesUiState> = combine(spending, earliestMonth) { (month, spending), earliest ->
        CategoriesUiState(
            month = month,
            spending = spending,
            isPreviousEnabled = earliest != null && month.isAfter(earliest),
            isNextEnabled = month.isBefore(currentMonth)
        )
    }
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoriesUiState(month = currentMonth))

    fun previousMonth() {
        selectedMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        selectedMonth.update { minOf(it.plusMonths(1), currentMonth) }
    }

    fun goToCurrentMonth() {
        selectedMonth.value = currentMonth
    }
}
