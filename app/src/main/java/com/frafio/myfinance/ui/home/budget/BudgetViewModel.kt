package com.frafio.myfinance.ui.home.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.Income
import com.frafio.myfinance.data.repositories.IncomeRepository
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.storages.PurchaseStorage

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )
    private val incomeRepository = IncomeRepository(
        (application as MyFinanceApplication).incomeManager
    )

    var listener: BudgetListener? = null

    private val _isIncomesEmpty = MutableLiveData<Boolean>()
    val isIncomesEmpty: LiveData<Boolean>
        get() = _isIncomesEmpty

    private val _incomes = MutableLiveData<List<Income>>()
    val incomes: LiveData<List<Income>>
        get() = _incomes

    private val _annualBudget = MutableLiveData<Double>()
    val annualBudget: LiveData<Double>
        get() = _annualBudget

    fun updateAnnualBudget(value: Double) {
        _annualBudget.value = value
    }

    fun updateIncomeList(limit: Long) {
        val response = incomeRepository.updateIncomeList(limit)
        listener?.onCompleted(response, null)
    }

    fun updateIncomeNumber() {
        val response = purchaseRepository.getPurchaseNumber(DbPurchases.FIELDS.INCOMES.value)
        listener?.onCompleted(response, null)
    }

    fun updateLocalIncomeList() {
        val incomes = incomeRepository.getIncomeList()
        _incomes.postValue(incomes)
        _isIncomesEmpty.postValue(incomes.isEmpty())
    }

    fun setMonthlyBudget(budget: Double, getOldBudget: Boolean = false) {
        val previousBudget = if (getOldBudget) PurchaseStorage.monthlyBudget.value ?: 0.0 else null
        val response = purchaseRepository.setMonthlyBudget(budget)
        listener?.onCompleted(response, previousBudget)
    }

    fun deleteIncomeAt(position: Int, income: Income) {
        val response = incomeRepository.deleteIncomeAt(position)
        listener?.onDeleteCompleted(response, income)
    }

    fun addIncome(income: Income) {
        val response = incomeRepository.addIncome(income)
        listener?.onCompleted(response, null)
    }
}