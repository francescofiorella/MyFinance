package com.frafio.myfinance.data.manager

interface FetchListener {
    fun onFetchSuccess()

    fun onFetchFailure(message: String)
}