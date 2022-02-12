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

    fun getTotals(purchases: List<Purchase>): Pair<List<Purchase>, HashMap<Int, MutableList<Purchase>>> {
        val totals: MutableList<Purchase> = mutableListOf()
        val map: HashMap<Int, MutableList<Purchase>> = hashMapOf()
        var index = -1
        purchases.forEach { purchase ->
            if (purchase.type == 0) {
                totals.add(purchase)
                index++
                map[index] = mutableListOf()
            } else {
                map[index]?.add(purchase)
            }
        }
        return Pair(totals, map)
    }
}