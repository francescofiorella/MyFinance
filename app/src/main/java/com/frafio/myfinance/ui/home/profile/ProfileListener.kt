package com.frafio.myfinance.ui.home.profile

import androidx.lifecycle.LiveData
import com.frafio.myfinance.data.model.AuthResult

interface ProfileListener {

    fun onStarted(notify: Boolean = true)

    fun onFullNameUpdateComplete(response: LiveData<AuthResult>, previousFullName: String, notify: Boolean = true)

    fun onProPicUpdateComplete(response: LiveData<AuthResult>)

    fun onDynamicColorChanged()
}