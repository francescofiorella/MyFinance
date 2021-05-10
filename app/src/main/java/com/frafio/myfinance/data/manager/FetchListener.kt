package com.frafio.myfinance.data.manager

interface FetchListener {
    fun onFetchSuccess(message: String?)

    fun onFetchFailure(message: String)
}