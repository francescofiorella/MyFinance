package com.frafio.myfinance.ui.home.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.LocalPurchaseRepository
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.doubleToString
import java.time.LocalDate

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )
    private val localPurchaseRepository = LocalPurchaseRepository()

    var monthShown = true
    var thisMonthSum = 0.0
    var thisYearSum = 0.0

    val isListEmpty = MutableLiveData<Boolean?>(null)

    private val _lastYearPurchases = MutableLiveData<List<Purchase>>()
    val lastYearPurchases: LiveData<List<Purchase>>
        get() = _lastYearPurchases

    private val _monthlyBudget = MutableLiveData<String>()
    val monthlyBudget: LiveData<String>
        get() = _monthlyBudget

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

    fun getPricesList(): LiveData<List<Double?>> {
        return localPurchaseRepository.getPricesPerInterval(2033, 2024, 8, 7)
    }

    fun updateStats() {
        purchaseRepository.getLastYearPurchases(_lastYearPurchases)
        _monthlyBudget.value = doubleToString(purchaseRepository.getMonthlyBudgetFromStorage())
    }
}