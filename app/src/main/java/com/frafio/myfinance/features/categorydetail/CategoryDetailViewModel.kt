package com.frafio.myfinance.features.categorydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.model.ExpenseCategories
import com.frafio.myfinance.core.data.model.ExpenseCategory
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.data.model.categoriesTotal
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.PendingExpensesFilter
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers.Default
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.core.utils.MONTH_RANGE_COUNTS
import com.frafio.myfinance.core.utils.availableMonthRanges
import com.frafio.myfinance.core.utils.monthRangeCount
import com.frafio.myfinance.core.utils.monthsToLoad
import com.frafio.myfinance.core.utils.shownMonthRange
import com.frafio.myfinance.core.utils.trackedMonths
import com.frafio.myfinance.core.utils.trackingStart
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    /** The first month of the [TREND_MONTHS] window ending with [month]. */
    val windowStart: LocalDate = month.minusMonths(TREND_MONTHS - 1L)

    /** Null until loaded. */
    val stats: StateFlow<CategoryDetailStats?> = run {
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

    private val _trendRange = MutableStateFlow(0)

    /** The trend chart's range: an index into [MONTH_RANGE_COUNTS], Auto first. */
    val trendRange: StateFlow<Int> = _trendRange.asStateFlow()

    // How many bars fit the trend chart's width, used by the Auto range.
    private val _trendVisibleItems = MutableStateFlow(6)

    /**
     * The first month the trend has data for: the earliest expense of any category, so a category's
     * empty months after it are real zeros. Older months show as no data. Null until loaded.
     */
    val trendTrackingStart: StateFlow<LocalDate?> = expensesLocalRepository.getEarliestYearMonth()
        .map { trackingStart(it, month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** The ranges worth offering for the months tracked up to [month]. */
    val trendAvailableRanges: StateFlow<List<Int>> = combine(trendTrackingStart, _trendVisibleItems) { start, visibleItems ->
        start?.let { availableMonthRanges(trackedMonths(it, month), visibleItems) } ?: ALL_MONTH_RANGES
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ALL_MONTH_RANGES)

    /** How many months the trend chart shows. */
    val trendShownCount: StateFlow<Int> = combine(
        _trendRange,
        _trendVisibleItems,
        trendAvailableRanges
    ) { range, visibleItems, available ->
        monthRangeCount(shownMonthRange(range, available), visibleItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _trendVisibleItems.value)

    /** The category's monthly totals up to [month], enough for every range, oldest first. */
    val trend: StateFlow<List<MonthlyTotal>> = _trendVisibleItems
        .map(::monthsToLoad)
        .distinctUntilChanged()
        .flatMapLatest { count ->
            val start = month.minusMonths(count - 1L)
            val end = month.plusMonths(1)
            expensesLocalRepository.getExpensesOfCategory(category.id, start.year, start.monthValue, end.year, end.monthValue)
                .map { expenses -> monthlyTotals(expenses, lastMonth = month, count = count) }
        }
        .flowOn(defaultDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTrendRange(index: Int) {
        _trendRange.value = index.coerceIn(MONTH_RANGE_COUNTS.indices)
    }

    fun setTrendVisibleItems(count: Int) {
        _trendVisibleItems.value = count
    }

    /** Asks the Expenses tab to show only this category; the caller then navigates there. */
    fun onSeeAllExpenses() {
        pendingExpensesFilter.request(category.id)
    }
}
