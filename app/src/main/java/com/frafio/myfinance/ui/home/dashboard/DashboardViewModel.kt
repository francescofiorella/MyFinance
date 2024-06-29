package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.doubleToPrice
import com.frafio.myfinance.utils.doubleToPriceWithoutDecimals
import com.frafio.myfinance.utils.doubleToString

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var monthShown = true

    private val _lastYearPurchases = MutableLiveData<List<Purchase>>()
    val lastYearPurchases: LiveData<List<Purchase>>
        get() = _lastYearPurchases

    private val _purchaseListSize = MutableLiveData<Int>()
    val purchaseListSize: LiveData<Int>
        get() = _purchaseListSize

    private val _monthlyBudget = MutableLiveData<String>()
    val monthlyBudget: LiveData<String>
        get() = _monthlyBudget

    private val _todayTotString = MutableLiveData<String>()
    val todayTotString: LiveData<String>
        get() = _todayTotString

    private val _thisYearString = MutableLiveData<String>()
    val thisYearString: LiveData<String>
        get() = _thisYearString

    private val _thisMonthString = MutableLiveData<String>()
    val thisMonthString: LiveData<String>
        get() = _thisMonthString

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
        _purchaseListSize.value = purchaseRepository.purchaseListSize()
        purchaseRepository.getLastYearPurchases(_lastYearPurchases)
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