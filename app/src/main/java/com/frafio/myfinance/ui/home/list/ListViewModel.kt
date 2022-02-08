package com.frafio.myfinance.ui.home.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository

class ListViewModel(private val purchaseRepository: PurchaseRepository) : ViewModel() {
    var listener: DeleteListener? = null

    val purchaseListSize = purchaseRepository.purchaseListSize()

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>>
        get() = _purchases

    fun getPurchases() {
        val purchases = purchaseRepository.getPurchaseList()
        _purchases.value = purchases
    }

    fun getPurchaseList(): List<Purchase> {
        return purchaseRepository.getPurchaseList()
    }

    fun deletePurchaseAt(position: Int) {
        val response = purchaseRepository.deletePurchaseAt(position)
        listener?.onDeleteComplete(response)
    }
}