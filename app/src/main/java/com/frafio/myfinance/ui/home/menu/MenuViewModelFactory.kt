package com.frafio.myfinance.ui.home.menu

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frafio.myfinance.data.repositories.PurchaseRepository

@Suppress("UNCHECKED_CAST")
class MenuViewModelFactory(
    private val purchaseRepository: PurchaseRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MenuViewModel(purchaseRepository, sharedPreferences) as T
    }
}