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

    fun updatePurchaseList() {
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

    fun updateType(purchase: Purchase, newType: Int, position: Int) {
        val updated = Purchase(
            email = purchase.email,
            name = purchase.name,
            price = purchase.price,
            year = purchase.year,
            month = purchase.month,
            day = purchase.day,
            type = newType,
            id = purchase.id,
            category = purchase.category
        )
        val response = purchaseRepository.editPurchase(updated, position)
        listener?.onUpdateTypeComplete(response)
    }

    fun addPurchase(purchase: Purchase) {
        val response = purchaseRepository.addPurchase(purchase)
        listener?.onDeleteCancelComplete(response)
    }
}