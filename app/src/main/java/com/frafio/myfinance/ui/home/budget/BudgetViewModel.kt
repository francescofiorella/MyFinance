package com.frafio.myfinance.ui.home.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repositories.PurchaseRepository

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var listener: BudgetListener? = null

    private val _monthlyBudget = MutableLiveData<Double>()
    val monthlyBudget: LiveData<Double>
        get() = _monthlyBudget

    private val _annualBudget = MutableLiveData<Double>()
    val annualBudget: LiveData<Double>
        get() = _annualBudget

    fun updateMonthlyBudgetFromStorage() {
        _monthlyBudget.value = purchaseRepository.getMonthlyBudgetFromStorage()
        _annualBudget.value = purchaseRepository.getMonthlyBudgetFromStorage() * 12.0
    }

    fun getMonthlyBudgetFromDb() {
        val response = purchaseRepository.getMonthlyBudget()
        listener?.onCompleted(response, null)
    }

    fun updateMonthlyBudget(budget: Double, getOldBudget: Boolean = false) {
        val previousBudget = if (getOldBudget) monthlyBudget.value ?: 0.0 else null
        val response = purchaseRepository.updateMonthlyBudget(budget)
        listener?.onCompleted(response, previousBudget)
    }
}