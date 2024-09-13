package com.frafio.myfinance.ui.home.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.repository.IncomeRepository
import com.frafio.myfinance.data.repository.IncomesLocalRepository
import com.frafio.myfinance.data.repository.ExpensesRepository
import com.frafio.myfinance.data.storage.MyFinanceStorage

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val expensesRepository = ExpensesRepository(
        (application as MyFinanceApplication).expensesManager
    )
    private val incomeRepository = IncomeRepository(
        (application as MyFinanceApplication).incomesManager
    )
    private val incomesLocalRepository = IncomesLocalRepository()

    var listener: BudgetListener? = null

    private val _isIncomesEmpty = MutableLiveData<Boolean>()
    val isIncomesEmpty: LiveData<Boolean>
        get() = _isIncomesEmpty

    private val _annualBudget = MutableLiveData<Double>()
    val annualBudget: LiveData<Double>
        get() = _annualBudget

    fun updateAnnualBudget(value: Double) {
        _annualBudget.value = value
    }

    fun updateIncomesEmpty(isListEmpty: Boolean) {
        _isIncomesEmpty.postValue(isListEmpty)
    }

    fun setMonthlyBudget(budget: Double, getOldBudget: Boolean = false) {
        val previousBudget = if (getOldBudget) MyFinanceStorage.monthlyBudget.value ?: 0.0 else null
        val response = expensesRepository.setMonthlyBudget(budget)
        listener?.onCompleted(response, previousBudget)
    }

    fun getLocalIncomes(): LiveData<List<Income>> {
        return incomesLocalRepository.getAll()
    }

    fun deleteIncome(income: Income) {
        val response = incomeRepository.deleteIncome(income)
        listener?.onDeleteCompleted(response, income)
    }

    fun addIncome(income: Income) {
        val response = incomeRepository.addIncome(income)
        listener?.onCompleted(response, null)
    }
}