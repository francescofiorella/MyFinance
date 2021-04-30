package com.frafio.myfinance.ui.auth

import androidx.lifecycle.LiveData

interface AuthListener {
    fun onStarted()

    // 1: success, 2: invalid email, 3: wrong password, 4: user not fount
    // String: reset password and some other failure
    fun onSuccess(response: LiveData<Any>)

    // 1: empty email, 2: empty password, 3: short password
    fun onFailure(errorCode: Int)
}