package com.frafio.myfinance.ui.home.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository

class ListViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var listener: DeleteListener? = null

    private val _purchaseListSize = MutableLiveData<Int>()
    val purchaseListSize: LiveData<Int>
        get() = _purchaseListSize

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>>
        get() = _purchases

    fun getPurchases() {
        val purchases = purchaseRepository.getPurchaseList()
        _purchases.postValue(purchases)
    }

    fun getPurchaseList(): List<Purchase> {
        return purchaseRepository.getPurchaseList()
    }

    fun deletePurchaseAt(position: Int) {
        val response = purchaseRepository.deletePurchaseAt(position)
        listener?.onDeleteComplete(response)
    }

    fun updateListSize() {
        _purchaseListSize.value = purchaseRepository.purchaseListSize()
    }
}