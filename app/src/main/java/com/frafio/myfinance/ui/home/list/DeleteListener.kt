package com.frafio.myfinance.ui.home.list

import androidx.lifecycle.LiveData

interface DeleteListener {
    fun onDeleteComplete(response: LiveData<Any>)
}