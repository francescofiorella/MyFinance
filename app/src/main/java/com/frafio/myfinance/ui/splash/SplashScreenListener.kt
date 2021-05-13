package com.frafio.myfinance.ui.splash

import androidx.lifecycle.LiveData

interface SplashScreenListener {
    fun onComplete(response: LiveData<Any>)
}