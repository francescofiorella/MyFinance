package com.frafio.myfinance.ui.home.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.manager.FetchListener
import com.frafio.myfinance.data.manager.PurchaseManager
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository

class ListViewModel(
    private val repository: PurchaseRepository
) : ViewModel() {

    var fetchListener: FetchListener? = null

    val purchaseListSize = repository.purchaseListSize()

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>>
        get() = _purchases

    fun getPurchases() {
        val purchases = PurchaseManager.getPurchaseList()
        _purchases.value = purchases
    }
}