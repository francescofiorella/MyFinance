package com.frafio.myfinance.ui.home.menu

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.PurchaseResult

interface MenuListener {
    fun onStarted()

    fun <T> onCompleted(result: LiveData<T>)
}