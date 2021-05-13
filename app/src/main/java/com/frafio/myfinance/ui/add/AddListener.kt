package com.frafio.myfinance.ui.add

import androidx.lifecycle.LiveData

interface AddListener {

    fun onAddStart()

    fun onAddSuccess(response: LiveData<Any>)

    // 1: nome vuoto, 2: nome="Totale", 3: prezzo vuoto
    fun onAddFailure(errorCode: Int)
}