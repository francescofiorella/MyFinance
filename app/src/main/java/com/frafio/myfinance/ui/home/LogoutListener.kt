package com.frafio.myfinance.ui.home

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.AuthResult

interface LogoutListener {
    fun onLogOutSuccess(response: LiveData<AuthResult>)
}