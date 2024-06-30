package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.doubleToString

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var monthShown = true
    var todaySum = 0.0
    var thisMonthSum = 0.0
    var thisYearSum = 0.0

    val isLayoutReady = MutableLiveData(false)

    private val _lastYearPurchases = MutableLiveData<List<Purchase>>()
    val lastYearPurchases: LiveData<List<Purchase>>
        get() = _lastYearPurchases

    private val _monthlyBudget = MutableLiveData<String>()
    val monthlyBudget: LiveData<String>
        get() = _monthlyBudget

    fun updateStats() {
        purchaseRepository.getLastYearPurchases(_lastYearPurchases)
        _monthlyBudget.value = doubleToString(purchaseRepository.getMonthlyBudgetFromStorage())
    }
}