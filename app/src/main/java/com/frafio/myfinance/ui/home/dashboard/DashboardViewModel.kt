package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.models.BarChartEntry
import com.frafio.myfinance.data.repositories.LocalPurchaseRepository
import com.frafio.myfinance.utils.dateToUTCTimestamp
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val localPurchaseRepository = LocalPurchaseRepository()

    var monthShown = true
    var thisMonthSum = 0.0
    var thisYearSum = 0.0
    var monthlyBudget = 0.0

    val isListEmpty = MutableLiveData<Boolean?>(null)


    private val _lastDateForBarChart = MutableLiveData(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()))
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
        val today = LocalDate.now()
        return localPurchaseRepository.getPriceSumFromDay(
            today.year,
            today.monthValue,
            today.dayOfMonth
        )
    }

    fun getPriceSumFromThisMonth(): LiveData<Double?> {
        val today = LocalDate.now()
        return localPurchaseRepository.getPriceSumFromMonth(today.year, today.monthValue)
    }

    fun getPriceSumFromThisYear(): LiveData<Double?> {
        val today = LocalDate.now()
        return localPurchaseRepository.getPriceSumFromYear(today.year)
    }

    fun getPricesList(): LiveData<List<BarChartEntry>> {
        var date = _lastDateForBarChart.value!!.plusMonths(1)
        val lastTimestamp = dateToUTCTimestamp(date.year, date.monthValue, date.dayOfMonth)
        date = date.minusYears(1)
        val firstTimestamp = dateToUTCTimestamp(date.year, date.monthValue, date.dayOfMonth)
        return localPurchaseRepository.getPurchasesAfterAndBefore(firstTimestamp, lastTimestamp)
    }
}