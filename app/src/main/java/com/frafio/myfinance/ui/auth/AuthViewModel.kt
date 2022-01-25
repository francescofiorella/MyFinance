package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.repositories.UserRepository

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var email: String? = null
    var password: String? = null

    var fullName: String? = null
    var passwordConfirm: String? = null

    var authListener: AuthListener? = null

    fun onLoginButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
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

    fun onResetButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        authListener?.onAuthStarted()

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_EMAIL))
            return
        }

        val resetResponse = userRepository.resetPassword(email!!)
        authListener?.onAuthSuccess(resetResponse)
    }

    fun onGoogleRequest(data: Intent?) {
        authListener?.onAuthStarted()

        val googleResponse = userRepository.userLogin(data)
        authListener?.onAuthSuccess(googleResponse)
    }

    fun onSignupButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
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

    fun updateUserData() {
        val response = userRepository.updateUserData()
        authListener?.onAuthSuccess(response)
    }

    fun getUserName(): String {
        return userRepository.getUser()!!.fullName!!
    }
}