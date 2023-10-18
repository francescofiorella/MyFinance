package com.frafio.myfinance.ui.home.menu

import androidx.lifecycle.LiveData

interface MenuListener {
    fun onStarted()

    fun <T> onCompleted(result: LiveData<T>)
}