package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.UserRepository

class AuthViewModel(
        private val repository: UserRepository
) : ViewModel() {

    var email: String? = null
    var password: String? = null

    var fullName: String? = null
    var passwordAgain: String? = null

    var authListener: AuthListener? = null

    fun onLoginButtonClick(view: View) {
        authListener?.onAuthStarted()

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(1)
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(2)
            return
        }

        if (password!!.length < 8) {
            authListener?.onAuthFailure(3)
            return
        }

        val loginResponse = repository.userLogin(email!!, password!!)
        authListener?.onAuthSuccess(loginResponse)
    }

    fun onResetButtotClick(view: View) {
        authListener?.onAuthStarted()

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(1)
            return
        }

        val resetResponse = repository.resetPassword(email!!)
        authListener?.onAuthSuccess(resetResponse)
    }

    fun onGoogleRequest(data: Intent?) {
        authListener?.onAuthStarted()

        val googleResponse = repository.userLogin(data)
        authListener?.onAuthSuccess(googleResponse)
    }

    fun onSignupButtonClick(view: View) {
        authListener?.onAuthStarted()

        // controlla la info aggiunte
        if (fullName.isNullOrEmpty()) {
            authListener?.onAuthFailure(1)
            return
        }

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(2)
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(3)
            return
        }

        if (password!!.length < 8) {
            authListener?.onAuthFailure(4)
            return
        }

        if (passwordAgain.isNullOrEmpty()) {
            authListener?.onAuthFailure(5)
            return
        }

        if (passwordAgain != password) {
            authListener?.onAuthFailure(6)
            return
        }

        val signupResponse = repository.userSignup(fullName!!, email!!, password!!)
        authListener?.onAuthSuccess(signupResponse)
    }
}