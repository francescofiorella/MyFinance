package com.frafio.myfinance.ui.auth

import androidx.lifecycle.LiveData

interface AuthListener {
    fun onStarted()

    // onLogin:
    // 1: success, 2: invalid email, 3: wrong password, 4: user not found, String: snackbar messages
    // onSignup:
    // 1: success, 2: weak password, 3: wrong email, 4: email already exist, String: snackbar messages
    fun onSuccess(response: LiveData<Any>)

    // onLogin:
    // 1: empty email, 2: empty password, 3: short password
    // onSignup:
    // 1: empty name, 2: empty email, 3: empty password, 4: short password, 5: empty passwordAgain, 6: wrong password
    fun onFailure(errorCode: Int)
}