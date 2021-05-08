package com.frafio.myfinance.ui.home.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.manager.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository

class ListViewModel(
    repository: PurchaseRepository
) : ViewModel() {

    val warningVisibility = repository.warningVisibility()
    val listVisibility = repository.purchaseVisibility()

    private val _purchases = MutableLiveData<List<Pair<String, Purchase>>>()
    val purchases: LiveData<List<Pair<String, Purchase>>>
        get() = _purchases

    fun getPurchases() {
        val purchases = PurchaseManager.getPurchaseList()
        _purchases.value = purchases
    }
}