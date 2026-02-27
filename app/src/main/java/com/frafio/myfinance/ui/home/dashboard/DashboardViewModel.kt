package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.data.repository.IncomesLocalRepository
import com.frafio.myfinance.utils.dateToUTCTimestamp
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val expensesLocalRepository = ExpensesLocalRepository()
    private val incomesLocalRepository = IncomesLocalRepository()
    private val today = LocalDate.now()

    var monthShown = true
    var thisMonthSum = 0.0
    var thisYearSum = 0.0
    var monthlyBudget = 0.0
    var incomesSum = 0.0
    var expensesSum = 0.0

    private val _balanceYearShown = MutableLiveData(
        today.year
    )
    val balanceYearShown: LiveData<Int>
        get() = _balanceYearShown

    val isListEmpty = MutableLiveData<Boolean?>(null)

    private var _monthlyShownInPieChart = true
    val monthlyShownInPieChart
        get() = _monthlyShownInPieChart
    private val _monthlyDateForPieChart = MutableLiveData(today)
    private val _annualDateForPieChart = MutableLiveData(today)
    private val _pieChartDate = MutableLiveData(today)
    val pieChartDate: LiveData<LocalDate>
        get() = _pieChartDate

    private val _lastDateForBarChart = MutableLiveData(
        today.with(TemporalAdjusters.firstDayOfMonth())
    )

    val barChartData: LiveData<Pair<List<Double>, List<String>>> = _lastDateForBarChart.switchMap { date ->
        val nextMonth = date.plusMonths(1)
        val lastTimestamp = dateToUTCTimestamp(nextMonth.year, nextMonth.monthValue, nextMonth.dayOfMonth)
        val firstDate = nextMonth.minusYears(1)
        val firstTimestamp = dateToUTCTimestamp(firstDate.year, firstDate.monthValue, firstDate.dayOfMonth)
        expensesLocalRepository.getPriceSumAfterAndBefore(firstTimestamp, lastTimestamp).map { entries ->
            val labels = mutableListOf<String>()
            val values = mutableListOf<Double>()
            var currentDate = date
            var j = 0
            repeat(12) {
                if (j < entries.size
                    && entries[j].year == currentDate.year
                    && entries[j].month == currentDate.monthValue
                ) {
                    val monthString = if (entries[j].month < 10)
                        "0${entries[j].month}" else entries[j].month.toString()
                    labels.add("$monthString/${entries[j].year - 2000}")
                    values.add(entries[j].value)
                    j++
                } else {
                    val monthString = if (currentDate.monthValue < 10)
                        "0${currentDate.monthValue}" else currentDate.monthValue.toString()
                    labels.add("$monthString/${currentDate.year - 2000}")
                    values.add(0.0)
                }
                currentDate = currentDate.minusMonths(1)
            }
            values.reversed() to labels.reversed()
        }
    }

    fun nextBalanceYear() {
        _balanceYearShown.postValue(_balanceYearShown.value!! + 1)
    }

    fun previousBalanceYear() {
        _balanceYearShown.postValue(_balanceYearShown.value!! - 1)
    }

    fun nextBarChartDate() {
        _lastDateForBarChart.postValue(_lastDateForBarChart.value!!.plusMonths(1))
    }

    fun previousBarChartDate() {
        _lastDateForBarChart.postValue(_lastDateForBarChart.value!!.minusMonths(1))
    }

    fun getExpensesNumber(): LiveData<Int> {
        return expensesLocalRepository.getCount()
    }

    fun getPriceSumFromToday(): LiveData<Double?> {
        return expensesLocalRepository.getPriceSumFromDay(
            today.year,
            today.monthValue,
            today.dayOfMonth
        )
    }

    fun getPriceSumFromThisMonth(): LiveData<Double?> {
        return expensesLocalRepository.getPriceSumFromMonth(today.year, today.monthValue)
    }

    fun getPriceSumFromThisYear(): LiveData<Double?> {
        return expensesLocalRepository.getPriceSumFromYear(today.year)
    }

    fun getExpensesOfMonth(): LiveData<List<Expense>> =
        expensesLocalRepository.getExpensesOfMonth(
            year = _pieChartDate.value!!.year,
            month = _pieChartDate.value!!.monthValue
        )

    fun getExpensesOfYear(): LiveData<List<Expense>> =
        expensesLocalRepository.getExpensesOfYear(
            year = _pieChartDate.value!!.year
        )

    fun getExpensesSumForBalance(): LiveData<Double?> {
        return expensesLocalRepository.getPriceSumFromYear(balanceYearShown.value!!)
    }

    fun getIncomesSumForBalance(): LiveData<Double?> {
        return incomesLocalRepository.getPriceSumFromYear(balanceYearShown.value!!)
    }

    fun switchPieChartData(setMonthly: Boolean) {
        _monthlyShownInPieChart = setMonthly
        if (setMonthly) {
            _pieChartDate.postValue(_monthlyDateForPieChart.value)
        } else {
            _pieChartDate.postValue(_annualDateForPieChart.value)
        }
    }

    fun nextPieChartDate() {
        if (_monthlyShownInPieChart) {
            _monthlyDateForPieChart.value = _pieChartDate.value!!.plusMonths(1)
            _pieChartDate.postValue(_monthlyDateForPieChart.value)
        } else {
            _annualDateForPieChart.value = _pieChartDate.value!!.plusYears(1)
            _pieChartDate.postValue(_annualDateForPieChart.value)
        }
    }

    fun previousPieChartDate() {
        if (_monthlyShownInPieChart) {
            _monthlyDateForPieChart.value = _pieChartDate.value!!.minusMonths(1)
            _pieChartDate.postValue(_monthlyDateForPieChart.value)
        } else {
            _annualDateForPieChart.value = _pieChartDate.value!!.minusYears(1)
            _pieChartDate.postValue(_annualDateForPieChart.value)
        }
    }
}
