package com.frafio.myfinance.ui.home

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.PurchaseResult

interface HomeListener {
    fun onLogOutSuccess(response: LiveData<AuthResult>)

    fun onSplashOperationComplete(response: LiveData<AuthResult>)

    fun onUserDataUpdated(response: LiveData<PurchaseResult>)
}