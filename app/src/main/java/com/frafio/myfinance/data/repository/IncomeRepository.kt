package com.frafio.myfinance.data.repository

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.manager.IncomeManager
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.data.model.PurchaseResult

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