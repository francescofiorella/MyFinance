package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.utils.doubleToString

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    private val _purchaseListSize = MutableLiveData<Int>()
    val purchaseListSize: LiveData<Int>
        get() = _purchaseListSize

    private val _monthlyBudget = MutableLiveData<String>()
    val monthlyBudget: LiveData<String>
        get() = _monthlyBudget

    private val _monthAvgString = MutableLiveData<String>()
    val monthAvgString: LiveData<String>
        get() = _monthAvgString

    private val _todayTotString = MutableLiveData<String>()
    val todayTotString: LiveData<String>
        get() = _todayTotString

    private val _totString = MutableLiveData<String>()
    val totString: LiveData<String>
        get() = _totString

    private val _lastMonthString = MutableLiveData<String>()
    val lastMonthString: LiveData<String>
        get() = _lastMonthString

    private val _rentTotString = MutableLiveData<String>()
    val rentTotString: LiveData<String>
        get() = _rentTotString

    private val _shoppingTotString = MutableLiveData<String>()
    val shoppingTotString: LiveData<String>
        get() = _shoppingTotString

    private val _transportTotString = MutableLiveData<String>()
    val transportTotString: LiveData<String>
        get() = _transportTotString

    private val _totalSumResult = MutableLiveData<Pair<PurchaseCode, Double>>()
    val totalSumResult: LiveData<Pair<PurchaseCode, Double>>
        get() = _totalSumResult

    private val _todayTotalResult = MutableLiveData<Pair<PurchaseCode, Double>>()
    val todayTotalResult: LiveData<Pair<PurchaseCode, Double>>
        get() = _todayTotalResult

    private val _thisMonthTotalResult = MutableLiveData<Pair<PurchaseCode, Double>>()
    val thisMonthTotalResult: LiveData<Pair<PurchaseCode, Double>>
        get() = _thisMonthTotalResult

    fun updateStats() {
        val stats = purchaseRepository.calculateStats()
        _purchaseListSize.value = purchaseRepository.purchaseListSize()
        _monthAvgString.value = stats[1]
        _rentTotString.value = stats[5]
        _shoppingTotString.value = stats[6]
        _transportTotString.value = stats[7]
        purchaseRepository.getSumPrices(_totalSumResult)
        purchaseRepository.getTodayTotal(_todayTotalResult)
        purchaseRepository.getThisMonthTotal(_thisMonthTotalResult)
        _monthlyBudget.value = doubleToString(purchaseRepository.getMonthlyBudgetFromStorage())
    }

    fun updateStats(
        totalSum: Double? = null,
        todayTot: Double? = null,
        thisMonthTotal: Double? = null
    ) {
        totalSum?.let {
            _totString.value = if (totalSum < 1000.0) {
                doubleToPrice(totalSum)
            } else {
                doubleToPriceWithoutDecimals(totalSum)
            }
        }
        todayTot?.let {
            _todayTotString.value = if (todayTot < 1000.0) {
                doubleToPrice(todayTot)
            } else {
                doubleToPriceWithoutDecimals(todayTot)
            }
        }
        thisMonthTotal?.let {
            _lastMonthString.value = doubleToPrice(thisMonthTotal)
        }
    }
}