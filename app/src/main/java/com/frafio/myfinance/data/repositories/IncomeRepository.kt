package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.managers.IncomeManager
import com.frafio.myfinance.data.models.Income
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.IncomeStorage
import com.frafio.myfinance.data.storages.MyFinanceDatabase

class IncomeRepository(private val incomeManager: IncomeManager) {
    private val incomeDao = MyFinanceDatabase.getDatabase(MyFinanceApplication.instance).incomeDao()

    fun getLocalIncomes(): List<Income> = incomeDao.getAll()

    fun getIncomeList(): List<Income> {
        return IncomeStorage.incomeList
    }

    fun updateIncomeList(limit: Long): LiveData<PurchaseResult> {
        return incomeManager.updateIncomeList(limit)
    }

    fun deleteIncomeAt(position: Int): LiveData<PurchaseResult> {
        return incomeManager.deleteIncomeAt(position)
    }

    fun addIncome(income: Income): LiveData<PurchaseResult> {
        return incomeManager.addIncome(income)
    }

    fun editIncome(
        income: Income,
        position: Int
    ): LiveData<PurchaseResult> {
        return incomeManager.editIncome(income, position)
    }
}