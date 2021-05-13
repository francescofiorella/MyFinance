package com.frafio.myfinance.ui.home.list.receipt

import androidx.lifecycle.LiveData

interface ReceiptListener {
    fun onLoadSuccess(response: LiveData<Any>)

    fun onLoadFailure(message: Any)
}