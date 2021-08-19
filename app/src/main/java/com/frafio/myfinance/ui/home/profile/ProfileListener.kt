package com.frafio.myfinance.ui.home.profile

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.models.AuthResult

interface ProfileListener {
    fun onLogOutSuccess(response: LiveData<AuthResult>)
}