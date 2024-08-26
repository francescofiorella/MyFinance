package com.frafio.myfinance.ui.home

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.model.FinanceResult

interface HomeListener {
    fun onLogOutSuccess(response: LiveData<AuthResult>)

    fun onSplashOperationComplete(response: LiveData<AuthResult>)

    fun onUserDataUpdated(response: LiveData<FinanceResult>)
}