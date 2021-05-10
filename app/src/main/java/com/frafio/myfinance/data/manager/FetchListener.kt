package com.frafio.myfinance.data.manager

import androidx.lifecycle.LiveData

interface FetchListener {
    fun onFetchSuccess(response: LiveData<Any>?)

    fun onFetchFailure(message: String)
}