package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repositories.PurchaseRepository

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    private val _purchaseListSize = MutableLiveData<Int>()
    val purchaseListSize: LiveData<Int>
        get() = _purchaseListSize

    private val _dayAvgString = MutableLiveData<String>()
    val dayAvgString: LiveData<String>
        get() = _dayAvgString

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

    fun getStats() {
        val stats = purchaseRepository.calculateStats()
        _purchaseListSize.value = purchaseRepository.purchaseListSize()
        _dayAvgString.value = stats[0]
        _monthAvgString.value = stats[1]
        _todayTotString.value = stats[2]
        _totString.value = stats[3]
        _lastMonthString.value = stats[4]
        _rentTotString.value = stats[5]
        _shoppingTotString.value = stats[6]
        _transportTotString.value = stats[7]
    }
}