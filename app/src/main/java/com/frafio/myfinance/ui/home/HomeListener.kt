package com.frafio.myfinance.ui.home

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.AuthResult

interface HomeListener {
    fun onLogOutSuccess(response: LiveData<AuthResult>)

    fun onSplashOperationComplete(response: LiveData<AuthResult>)
}