package com.frafio.myfinance.ui.store

import androidx.lifecycle.LiveData

interface StoreListener {
    fun onStoreStarted()

    fun onStoreSuccess(response: LiveData<Any>)

    fun onStoreFailure(errorCode: Int)
}