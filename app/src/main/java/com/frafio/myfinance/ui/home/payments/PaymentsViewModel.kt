package com.frafio.myfinance.ui.home.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.repositories.LocalPurchaseRepository
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.utils.dateToUTCTimestamp

class PaymentsViewModel(application: Application) : AndroidViewModel(application) {
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )
    private val localPurchaseRepository = LocalPurchaseRepository()

    var listener: PaymentListener? = null

    private val _isPurchasesEmpty = MutableLiveData<Boolean>()
    val isPurchasesEmpty: LiveData<Boolean>
        get() = _isPurchasesEmpty

    fun getLocalPurchases(): LiveData<List<Purchase>> {
        return localPurchaseRepository.getAll()
    }

    fun updatePurchasesEmpty(isListEmpty: Boolean) {
        _isPurchasesEmpty.postValue(isListEmpty)
    }

    fun deletePurchase(purchase: Purchase) {
        val response = purchaseRepository.deletePurchase(purchase)
        listener?.onDeleteCompleted(response, purchase)
    }

    fun updateCategory(purchase: Purchase, newCategory: Int) {
        val updated = Purchase(
            name = purchase.name,
            price = purchase.price,
            year = purchase.year,
            month = purchase.month,
            day = purchase.day,
            timestamp = dateToUTCTimestamp(purchase.year!!, purchase.month!!, purchase.day!!),
            category = newCategory,
            id = purchase.id
        )
        val response = purchaseRepository.editPurchase(updated)
        listener?.onCompleted(response)
    }

    fun addPurchase(purchase: Purchase) {
        val response = purchaseRepository.addPurchase(purchase)
        listener?.onCompleted(response)
    }
}