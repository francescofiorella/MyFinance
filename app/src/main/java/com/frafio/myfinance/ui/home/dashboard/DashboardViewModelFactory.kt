package com.frafio.myfinance.ui.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.data.repositories.PurchaseRepository

@Suppress("UNCHECKED_CAST")
class DashboardViewModelFactory(
    private val purchaseRepository: PurchaseRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(purchaseRepository) as T
    }
}