package com.frafio.myfinance.ui.auth

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.AuthResult

interface AuthListener {

    fun onAuthStarted()

    fun onAuthSuccess(response: LiveData<AuthResult>)

    fun onAuthFailure(authResult: AuthResult)
}