package com.frafio.myfinance.ui.home.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repositories.PurchaseRepository

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var monthlyBudget: Double? = purchaseRepository.getMonthlyBudget()
}