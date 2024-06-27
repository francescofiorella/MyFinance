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

    var monthShown = true

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

    private val _thisYearString = MutableLiveData<String>()
    val thisYearString: LiveData<String>
        get() = _thisYearString

    private val _thisMonthString = MutableLiveData<String>()
    val thisMonthString: LiveData<String>
        get() = _thisMonthString

    private val _rentTotString = MutableLiveData<String>()
    val rentTotString: LiveData<String>
        get() = _rentTotString

    private val _shoppingTotString = MutableLiveData<String>()
    val shoppingTotString: LiveData<String>
        get() = _shoppingTotString

    private val _transportTotString = MutableLiveData<String>()
    val transportTotString: LiveData<String>
        get() = _transportTotString

    private val _thisYearResult = MutableLiveData<Pair<PurchaseCode, Double>>()
    val thisYearResult: LiveData<Pair<PurchaseCode, Double>>
        get() = _thisYearResult

    private val _todayTotalResult = MutableLiveData<Pair<PurchaseCode, Double>>()
    val todayTotalResult: LiveData<Pair<PurchaseCode, Double>>
        get() = _todayTotalResult

    private val _thisMonthResult = MutableLiveData<Pair<PurchaseCode, Double>>()
    val thisMonthResult: LiveData<Pair<PurchaseCode, Double>>
        get() = _thisMonthResult

    fun updateStats() {
        val stats = purchaseRepository.calculateStats()
        _purchaseListSize.value = purchaseRepository.purchaseListSize()
        _monthAvgString.value = stats[1]
        _rentTotString.value = stats[5]
        _shoppingTotString.value = stats[6]
        _transportTotString.value = stats[7]
        purchaseRepository.getThisYearTotal(_thisYearResult)
        purchaseRepository.getTodayTotal(_todayTotalResult)
        purchaseRepository.getThisMonthTotal(_thisMonthResult)
        _monthlyBudget.value = doubleToString(purchaseRepository.getMonthlyBudgetFromStorage())
    }

    fun updateStats(
        thisYearTot: Double? = null,
        todayTot: Double? = null,
        thisMonthTot: Double? = null
    ) {
        thisYearTot?.let {
            _thisYearString.value = if (thisYearTot < 1000.0) {
                doubleToPrice(thisYearTot)
            } else {
                doubleToPriceWithoutDecimals(thisYearTot)
            }
        }
        todayTot?.let {
            _todayTotString.value = if (todayTot < 1000.0) {
                doubleToPrice(todayTot)
            } else {
                doubleToPriceWithoutDecimals(todayTot)
            }
        }
        thisMonthTot?.let {
            _thisMonthString.value = if (thisMonthTot < 1000.0) {
                doubleToPrice(thisMonthTot)
            } else {
                doubleToPriceWithoutDecimals(thisMonthTot)
            }
        }
    }
}