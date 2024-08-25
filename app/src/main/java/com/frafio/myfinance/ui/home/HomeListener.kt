package com.frafio.myfinance.ui.home

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.model.PurchaseResult

interface HomeListener {
    fun onLogOutSuccess(response: LiveData<AuthResult>)

    fun onSplashOperationComplete(response: LiveData<AuthResult>)

    fun onUserDataUpdated(response: LiveData<PurchaseResult>)
}