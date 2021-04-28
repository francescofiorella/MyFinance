package com.frafio.myfinance.ui.auth

interface AuthListener {
    fun onStarted()

    fun onSuccess()

    fun onFailure(message: String)
}