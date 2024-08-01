package com.frafio.myfinance.data.repositories

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.managers.IncomeManager
import com.frafio.myfinance.data.models.Income
import com.frafio.myfinance.data.models.PurchaseResult

class IncomeRepository(private val incomeManager: IncomeManager) {
    fun updateIncomeList(): LiveData<PurchaseResult> {
        return incomeManager.updateIncomeList()
    }

    fun addIncome(income: Income): LiveData<PurchaseResult> {
        return incomeManager.addIncome(income)
    }

    fun editIncome(income: Income): LiveData<PurchaseResult> {
        return incomeManager.editIncome(income)
    }

    fun deleteIncome(income: Income): LiveData<PurchaseResult> {
        return incomeManager.deleteIncome(income)
    }
}