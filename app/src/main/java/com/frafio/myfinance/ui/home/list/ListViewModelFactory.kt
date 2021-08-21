package com.frafio.myfinance.ui.home.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.data.repositories.PurchaseRepository

@Suppress("UNCHECKED_CAST")
class ListViewModelFactory(
    private val purchaseRepository: PurchaseRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ListViewModel(purchaseRepository) as T
    }
}