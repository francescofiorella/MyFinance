package com.frafio.myfinance.features.categorydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.data.model.categoriesTotal
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.PendingExpensesFilter
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers.Default
import com.frafio.myfinance.core.navigation.RootKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

@HiltViewModel(assistedFactory = CategoryDetailViewModel.Factory::class)
class CategoryDetailViewModel @AssistedInject constructor(
    expensesLocalRepository: ExpensesLocalRepository,
    private val pendingExpensesFilter: PendingExpensesFilter,
    @Dispatcher(Default) defaultDispatcher: CoroutineDispatcher,
    @Assisted navKey: RootKey.CategoryDetail
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: RootKey.CategoryDetail): CategoryDetailViewModel
    }

    val category: ExpenseCategory = ExpenseCategories.get(navKey.categoryId)

    /** The first day of the month the overview was showing. */
    val month: LocalDate = LocalDate.of(navKey.year, navKey.month, 1)

    /** Null until loaded. */
    val stats: StateFlow<CategoryDetailStats?> = run {
        val windowStart = month.minusMonths(TREND_MONTHS - 1L)
        val windowEnd = month.plusMonths(1)
        combine(
            expensesLocalRepository.getExpensesOfCategory(
                category.id,
                windowStart.year, windowStart.monthValue,
                windowEnd.year, windowEnd.monthValue
            ),
            expensesLocalRepository.getCategorySumsOfMonth(month.year, month.monthValue)
        ) { expenses, monthSums ->
            buildCategoryDetail(month, expenses, monthSums.categoriesTotal())
        }
            .flowOn(defaultDispatcher)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    /** Asks the Expenses tab to show only this category; the caller then navigates there. */
    fun onSeeAllExpenses() {
        pendingExpensesFilter.request(category.id)
    }
}
