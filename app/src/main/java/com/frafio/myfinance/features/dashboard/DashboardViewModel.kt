package com.frafio.myfinance.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.model.DatePoint
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.model.MonthlyTotal
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.utils.ALL_MONTH_RANGES
import com.frafio.myfinance.core.utils.MONTH_RANGE_COUNTS
import com.frafio.myfinance.core.utils.availableMonthRanges
import com.frafio.myfinance.core.utils.fillMonthlyGaps
import com.frafio.myfinance.core.utils.monthRangeCount
import com.frafio.myfinance.core.utils.monthsToLoad
import com.frafio.myfinance.core.utils.shownMonthRange
import com.frafio.myfinance.core.utils.trackedMonths
import com.frafio.myfinance.core.utils.trackingStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class AnnualBalanceData(
    val year: Int,
    val incomes: Double,
    val expenses: Double
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val expensesLocalRepository: ExpensesLocalRepository,
    private val incomesLocalRepository: IncomesLocalRepository,
    userPreferencesRepository: UserPreferencesRepository,
    loadingRepository: LoadingRepository
) : ViewModel() {

    val today: LocalDate = LocalDate.now()

    private val thisMonth: LocalDate = today.with(TemporalAdjusters.firstDayOfMonth())

    private val _monthShown = MutableStateFlow(true)
    val monthShown: StateFlow<Boolean> = _monthShown.asStateFlow()

    val isListEmpty: StateFlow<Boolean?> = combine(
        expensesLocalRepository.getCount(),
        incomesLocalRepository.getCount(),
        loadingRepository.isFirstSync
    ) { expensesCount, incomesCount, isFirstSync ->
        val isEmpty = expensesCount == 0 && incomesCount == 0
        if (isFirstSync && isEmpty) null else isEmpty
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = if (loadingRepository.isFirstSync.value) null else false
    )

    val thisMonthSum: StateFlow<Double> = expensesLocalRepository.getPriceSumFromMonth(today.year, today.monthValue)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val thisYearSum: StateFlow<Double> = expensesLocalRepository.getPriceSumFromYear(today.year)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val earliestExpenseDate: StateFlow<DatePoint?> = expensesLocalRepository.getEarliestYearMonth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val earliestIncomeDate: StateFlow<DatePoint?> = incomesLocalRepository.getEarliestYearMonth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val earliestFinancialDate: StateFlow<DatePoint?> = combine(
        earliestExpenseDate,
        earliestIncomeDate
    ) { earliestExpense, earliestIncome ->
        when {
            earliestExpense == null -> earliestIncome
            earliestIncome == null -> earliestExpense
            earliestExpense.year < earliestIncome.year -> earliestExpense
            earliestExpense.year > earliestIncome.year -> earliestIncome
            earliestExpense.month < earliestIncome.month -> earliestExpense
            else -> earliestIncome
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _balanceYearShown = MutableStateFlow(today.year)
    val annualBalanceData: StateFlow<AnnualBalanceData> = _balanceYearShown
        .flatMapLatest { year ->
            combine(
                incomesLocalRepository.getPriceSumFromYear(year),
                expensesLocalRepository.getPriceSumFromYear(year)
            ) { incomes, expenses ->
                AnnualBalanceData(year, incomes ?: 0.0, expenses ?: 0.0)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AnnualBalanceData(today.year, 0.0, 0.0)
        )

    val isPreviousBalanceYearEnabled: StateFlow<Boolean> = combine(
        _balanceYearShown,
        earliestFinancialDate
    ) { year, earliest ->
        earliest?.let {
            year > it.year
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val monthlyBudget: StateFlow<Double> = userPreferencesRepository.userPreferencesFlow
        .map { it.monthlyBudget }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), userPreferencesRepository.userPreferencesFlow.value.monthlyBudget)

    private val _monthlyShownInPieChart = MutableStateFlow(true)
    val monthlyShownInPieChart: StateFlow<Boolean> = _monthlyShownInPieChart.asStateFlow()

    private val _monthlyDateForPieChart = MutableStateFlow(today)
    private val _annualDateForPieChart = MutableStateFlow(today)

    private val _pieChartDate = MutableStateFlow(today)
    val pieChartDate: StateFlow<LocalDate> = _pieChartDate.asStateFlow()

    val isNextPieChartDateEnabled: StateFlow<Boolean> = combine(
        _pieChartDate,
        _monthlyShownInPieChart
    ) { date, isMonthly ->
        if (isMonthly) {
            date.isBefore(today.with(TemporalAdjusters.firstDayOfMonth()))
        } else {
            date.year < today.year
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isPreviousPieChartDateEnabled: StateFlow<Boolean> = combine(
        _pieChartDate,
        _monthlyShownInPieChart,
        earliestExpenseDate
    ) { date, isMonthly, earliest ->
        earliest?.let {
            if (isMonthly) {
                (date.year > it.year) || (date.year == it.year && date.monthValue > it.month)
            } else {
                date.year > it.year
            }
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _lastDateForBarChart = MutableStateFlow(
        today.with(TemporalAdjusters.firstDayOfMonth())
    )

    val isNextBarChartDateEnabled: StateFlow<Boolean> = _lastDateForBarChart
        .map { it.isBefore(today.with(TemporalAdjusters.firstDayOfMonth())) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _barChartRange = MutableStateFlow(0)
    val barChartRange: StateFlow<Int> = _barChartRange.asStateFlow()

    // How many bars fit the chart width, used by the dynamic range.
    private val _barChartVisibleItems = MutableStateFlow(6)

    /** The first month the bar chart has data for, counted up to this month; older months show as no data. Null until loaded. */
    val barChartTrackingStart: StateFlow<LocalDate?> = expensesLocalRepository.getEarliestYearMonth()
        .map { trackingStart(it, thisMonth) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** The ranges worth offering for the months tracked up to this month, so they don't change while stepping back. */
    val barChartAvailableRanges: StateFlow<List<Int>> = combine(barChartTrackingStart, _barChartVisibleItems) { start, visibleItems ->
        start?.let { availableMonthRanges(trackedMonths(it, thisMonth), visibleItems) } ?: ALL_MONTH_RANGES
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ALL_MONTH_RANGES)

    val barChartShownCount: StateFlow<Int> = combine(
        _barChartRange,
        _barChartVisibleItems,
        barChartAvailableRanges
    ) { range, visibleItems, available ->
        monthRangeCount(shownMonthRange(range, available), visibleItems)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _barChartVisibleItems.value)

    val isPreviousBarChartDateEnabled: StateFlow<Boolean> = combine(
        _lastDateForBarChart,
        barChartShownCount,
        earliestExpenseDate
    ) { date, shownCount, earliest ->
        earliest?.let {
            val oldestShown = date.minusMonths(shownCount.toLong() - 1)
            (oldestShown.year > it.year) || (oldestShown.year == it.year && oldestShown.monthValue > it.month)
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _scrollToTop = MutableSharedFlow<Unit>(replay = 0)
    val scrollToTop: SharedFlow<Unit> = _scrollToTop.asSharedFlow()

    /** The months of the longest range, or more if more fit the width, so switching range only changes how many the chart shows. */
    val barChartData: StateFlow<List<MonthlyTotal>> = combine(
        _lastDateForBarChart,
        _barChartVisibleItems.map(::monthsToLoad).distinctUntilChanged()
    ) { date, count ->
        date to count
    }.flatMapLatest { (date, count) ->
        val nextMonth = date.plusMonths(1)
        val firstDate = nextMonth.minusMonths(count.toLong())
        expensesLocalRepository.getPriceSumAfterAndBefore(
            firstDate.year, firstDate.monthValue,
            nextMonth.year, nextMonth.monthValue
        ).map { entries -> fillMonthlyGaps(entries, lastMonth = date, count = count) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pieChartExpenses: StateFlow<List<Expense>> = combine(
        _pieChartDate,
        _monthlyShownInPieChart
    ) { date, isMonthly ->
        date to isMonthly
    }.flatMapLatest { (date, isMonthly) ->
        if (isMonthly) {
            expensesLocalRepository.getExpensesOfMonth(date.year, date.monthValue)
        } else {
            expensesLocalRepository.getExpensesOfYear(date.year)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySum: StateFlow<Double> = expensesLocalRepository.getPriceSumFromDay(today.year, today.monthValue, today.dayOfMonth)
        .flatMapLatest {
            flowOf(it ?: 0.0)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun toggleMonthShown(monthShown: Boolean) {
        _monthShown.value = monthShown
    }


    fun nextBalanceYear() {
        if (_balanceYearShown.value < today.year) {
            _balanceYearShown.value += 1
        }
    }

    fun previousBalanceYear() {
        _balanceYearShown.value -= 1
    }

    fun todayBalanceYear() {
        _balanceYearShown.value = today.year
    }

    fun nextBarChartDate() {
        if (_lastDateForBarChart.value.isBefore(today.with(TemporalAdjusters.firstDayOfMonth()))) {
            _lastDateForBarChart.value = _lastDateForBarChart.value.plusMonths(1)
        }
    }

    fun previousBarChartDate() {
        _lastDateForBarChart.value = _lastDateForBarChart.value.minusMonths(1)
    }

    fun todayBarChartDate() {
        _lastDateForBarChart.value = today.with(TemporalAdjusters.firstDayOfMonth())
    }

    fun selectBarChartRange(index: Int) {
        _barChartRange.value = index.coerceIn(MONTH_RANGE_COUNTS.indices)
    }

    fun setBarChartVisibleItems(count: Int) {
        _barChartVisibleItems.value = count
    }

    fun switchPieChartData(setMonthly: Boolean) {
        _monthlyShownInPieChart.value = setMonthly
        if (setMonthly) {
            _pieChartDate.value = _monthlyDateForPieChart.value
        } else {
            _pieChartDate.value = _annualDateForPieChart.value
        }
    }

    fun nextPieChartDate() {
        if (_monthlyShownInPieChart.value) {
            if (_pieChartDate.value.isBefore(today.with(TemporalAdjusters.firstDayOfMonth()))) {
                _monthlyDateForPieChart.value = _pieChartDate.value.plusMonths(1)
                _pieChartDate.value = _monthlyDateForPieChart.value
            }
        } else {
            if (_pieChartDate.value.year < today.year) {
                _annualDateForPieChart.value = _pieChartDate.value.plusYears(1)
                _pieChartDate.value = _annualDateForPieChart.value
            }
        }
    }

    fun previousPieChartDate() {
        if (_monthlyShownInPieChart.value) {
            _monthlyDateForPieChart.value = _pieChartDate.value.minusMonths(1)
            _pieChartDate.value = _monthlyDateForPieChart.value
        } else {
            _annualDateForPieChart.value = _pieChartDate.value.minusYears(1)
            _pieChartDate.value = _annualDateForPieChart.value
        }
    }

    fun todayPieChartDate() {
        if (_monthlyShownInPieChart.value) {
            _monthlyDateForPieChart.value = today
            _pieChartDate.value = _monthlyDateForPieChart.value
        } else {
            _annualDateForPieChart.value = today
            _pieChartDate.value = _annualDateForPieChart.value
        }
    }

    fun scrollToTop() {
        viewModelScope.launch {
            _scrollToTop.emit(Unit)
        }
    }
}