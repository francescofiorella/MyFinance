package com.frafio.myfinance.data.manager

interface ManagerListener {
    fun onManagerSuccess()

    fun onManagerFailure(message: String)
}