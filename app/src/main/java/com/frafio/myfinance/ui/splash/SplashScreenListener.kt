package com.frafio.myfinance.ui.splash

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.AuthResult

interface SplashScreenListener {
    fun onComplete(response: LiveData<AuthResult>)
}