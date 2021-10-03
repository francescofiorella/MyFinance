package com.frafio.myfinance.ui.home.list

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult

interface DeleteListener {
    fun onDeleteComplete(response: LiveData<Triple<PurchaseResult, List<Purchase>, Int?>>)
}