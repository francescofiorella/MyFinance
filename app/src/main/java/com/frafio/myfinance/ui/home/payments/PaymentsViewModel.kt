package com.frafio.myfinance.ui.home.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.PurchaseRepository

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )

    var listener: PaymentListener? = null

    private val _purchaseListSize = MutableLiveData<Int>()
    val purchaseListSize: LiveData<Int>
        get() = _purchaseListSize

    private val _purchases = MutableLiveData<List<Purchase>>()
    val purchases: LiveData<List<Purchase>>
        get() = _purchases

    fun updateLocalPurchaseList() {
        val purchases = purchaseRepository.getPurchaseList()
        _purchases.postValue(purchases)
    }

    fun deletePurchaseAt(position: Int, purchase: Purchase) {
        val response = purchaseRepository.deletePurchaseAt(position)
        listener?.onDeleteComplete(response, purchase)
    }

    fun updateListSize() {
        _purchaseListSize.value = purchaseRepository.purchaseListSize()
    }

    fun updateCategory(purchase: Purchase, newCategory: Int, position: Int) {
        val updated = Purchase(
            name = purchase.name,
            price = purchase.price,
            year = purchase.year,
            month = purchase.month,
            day = purchase.day,
            category = newCategory,
            id = purchase.id
        )
        val response = purchaseRepository.editPurchase(updated, position)
        listener?.onUpdateComplete(response)
    }

    fun addPurchase(purchase: Purchase) {
        val response = purchaseRepository.addPurchase(purchase)
        listener?.onUpdateComplete(response)
    }

    fun updatePurchaseList(limit: Long) {
        val response = purchaseRepository.updatePurchaseList(limit)
        listener?.onUpdateComplete(response)
    }

    fun updatePurchaseNumber() {
        val response = purchaseRepository.getPurchaseNumber()
        listener?.onUpdateComplete(response)
    }
}