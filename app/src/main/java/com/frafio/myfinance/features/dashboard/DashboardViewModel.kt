package com.frafio.myfinance.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frafio.myfinance.core.data.model.BarChartEntry
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.utils.dateToUTCTimestamp
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
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val pieChartEntriesSize = 24
    private val today = LocalDate.now()

    private val _monthShown = MutableStateFlow(true)
    val monthShown: StateFlow<Boolean> = _monthShown.asStateFlow()

    val isListEmpty: StateFlow<Boolean?> = expensesLocalRepository.getCount()
        .map { it == 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val thisMonthSum: StateFlow<Double> = expensesLocalRepository.getPriceSumFromMonth(today.year, today.monthValue)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val thisYearSum: StateFlow<Double> = expensesLocalRepository.getPriceSumFromYear(today.year)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    val monthlyBudget: StateFlow<Double> = userPreferencesRepository.userPreferencesFlow
        .map { it.monthlyBudget }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    private val _lastDateForBarChart = MutableStateFlow(
        today.with(TemporalAdjusters.firstDayOfMonth())
    )

    val isNextBarChartDateEnabled: StateFlow<Boolean> = _lastDateForBarChart
        .map { it.isBefore(today.with(TemporalAdjusters.firstDayOfMonth())) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _scrollToTop = MutableSharedFlow<Unit>(replay = 0)
    val scrollToTop: SharedFlow<Unit> = _scrollToTop.asSharedFlow()

    val barChartData: StateFlow<List<BarChartEntry>> = _lastDateForBarChart
        .flatMapLatest { date ->
            val nextMonth = date.plusMonths(1)
            val lastTimestamp =
                dateToUTCTimestamp(nextMonth.year, nextMonth.monthValue, nextMonth.dayOfMonth)
            val firstDate = nextMonth.minusMonths(pieChartEntriesSize.toLong())
            val firstTimestamp =
                dateToUTCTimestamp(firstDate.year, firstDate.monthValue, firstDate.dayOfMonth)
            expensesLocalRepository.getPriceSumAfterAndBefore(firstTimestamp, lastTimestamp).flatMapLatest { entries ->
                val values = mutableListOf<BarChartEntry>()
                var currentDate = date
                var j = 0
                repeat(pieChartEntriesSize) {
                    if (j < entries.size
                        && entries[j].year == currentDate.year
                        && entries[j].month == currentDate.monthValue
                    ) {
                        values.add(BarChartEntry(
                            value = entries[j].value,
                            year = entries[j].year,
                            month = entries[j].month
                        ))
                        j++
                    } else {
                        values.add(BarChartEntry(
                            value = 0.0,
                            year = currentDate.year,
                            month = currentDate.monthValue
                        ))
                    }
                    currentDate = currentDate.minusMonths(1)
                }
                flowOf(values.reversed())
            }
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