package com.frafio.myfinance.ui.home.menu

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.PurchaseResult

interface MenuListener {
    fun onStarted()

    fun onCompleted(result: LiveData<PurchaseResult>)
}