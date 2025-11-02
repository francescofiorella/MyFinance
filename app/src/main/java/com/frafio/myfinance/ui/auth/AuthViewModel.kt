package com.frafio.myfinance.ui.auth

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.repository.UserRepository

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository((application as MyFinanceApplication).authManager)

    var email: String? = null
    var password: String? = null

    var fullName: String? = null
    var passwordConfirm: String? = null

    var authListener: AuthListener? = null

    var isSigningUp: Boolean = false

    fun onLoginButtonClick() {
        authListener?.onAuthStarted()

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_EMAIL))
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_PASSWORD))
            return
        }

        if (password!!.length < 8) {
            authListener?.onAuthFailure(AuthResult(AuthCode.SHORT_PASSWORD))
            return
        }

        val loginResponse = userRepository.userLogin(email!!, password!!)
        authListener?.onAuthSuccess(loginResponse)
    }

    fun resetPassword(email: String) {
        authListener?.onAuthStarted()
        val resetResponse = userRepository.resetPassword(email)
        authListener?.onAuthSuccess(resetResponse)
    }

    fun onGoogleRequest(data: Intent?) {
        authListener?.onAuthStarted()

        val googleResponse = userRepository.userLogin(data)
        authListener?.onAuthSuccess(googleResponse)
    }

    fun onSignupButtonClick() {
        authListener?.onAuthStarted()

        // check info
        if (fullName.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_NAME))
            return
        }

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_EMAIL))
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_PASSWORD))
            return
        }

        if (password!!.length < 8) {
            authListener?.onAuthFailure(AuthResult(AuthCode.SHORT_PASSWORD))
            return
        }

        if (passwordConfirm.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_PASSWORD_CONFIRM))
            return
        }

        if (passwordConfirm != password) {
            authListener?.onAuthFailure(AuthResult(AuthCode.PASSWORD_NOT_MATCH))
            return
        }

        val signupResponse = userRepository.userSignup(fullName!!, email!!, password!!)
        authListener?.onAuthSuccess(signupResponse)
    }

    fun getUserName(): String {
        return userRepository.getUser()!!.fullName!!
    }
}