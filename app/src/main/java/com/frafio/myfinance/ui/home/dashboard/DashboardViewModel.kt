package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.model.BarChartEntry
import com.frafio.myfinance.data.model.Purchase
import com.frafio.myfinance.data.repository.LocalPurchaseRepository
import com.frafio.myfinance.utils.dateToUTCTimestamp
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val localPurchaseRepository = LocalPurchaseRepository()
    private val today = LocalDate.now()

    var monthShown = true
    var thisMonthSum = 0.0
    var thisYearSum = 0.0
    var monthlyBudget = 0.0

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
    val lastDateForBarChart: LiveData<LocalDate>
        get() = _lastDateForBarChart

    fun nextBarChartDate() {
        _lastDateForBarChart.postValue(_lastDateForBarChart.value!!.plusMonths(1))
    }

    fun previousBarChartDate() {
        _lastDateForBarChart.postValue(_lastDateForBarChart.value!!.minusMonths(1))
    }

    fun getPurchaseNumber(): LiveData<Int> {
        return localPurchaseRepository.getCount()
    }

    fun getPriceSumFromToday(): LiveData<Double?> {
        return localPurchaseRepository.getPriceSumFromDay(
            today.year,
            today.monthValue,
            today.dayOfMonth
        )
    }

    fun getPriceSumFromThisMonth(): LiveData<Double?> {
        return localPurchaseRepository.getPriceSumFromMonth(today.year, today.monthValue)
    }

    fun getPriceSumFromThisYear(): LiveData<Double?> {
        return localPurchaseRepository.getPriceSumFromYear(today.year)
    }

    fun getPricesList(): LiveData<List<BarChartEntry>> {
        var date = _lastDateForBarChart.value!!.plusMonths(1)
        val lastTimestamp = dateToUTCTimestamp(date.year, date.monthValue, date.dayOfMonth)
        date = date.minusYears(1)
        val firstTimestamp = dateToUTCTimestamp(date.year, date.monthValue, date.dayOfMonth)
        return localPurchaseRepository.getPriceSumAfterAndBefore(firstTimestamp, lastTimestamp)
    }

    fun getPurchasesOfMonth(): LiveData<List<Purchase>> =
        localPurchaseRepository.getPurchasesOfMonth(
            year = _pieChartDate.value!!.year,
            month = _pieChartDate.value!!.monthValue
        )

    fun getPurchasesOfYear(): LiveData<List<Purchase>> =
        localPurchaseRepository.getPurchasesOfYear(
            year = _pieChartDate.value!!.year
        )

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