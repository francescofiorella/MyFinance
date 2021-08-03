package com.frafio.myfinance.ui.auth

import android.content.Intent
import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.enums.AUTH_RESULT
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.UserRepository

class AuthViewModel(
    private val userRepository: UserRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    var email: String? = null
    var password: String? = null

    var fullName: String? = null
    var passwordConfirm: String? = null

    var authListener: AuthListener? = null

    fun onLoginButtonClick(view: View) {
        authListener?.onAuthStarted()

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.EMPTY_EMAIL))
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.EMPTY_PASSWORD))
            return
        }

        if (password!!.length < 8) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.SHORT_PASSWORD))
            return
        }

        val loginResponse = userRepository.userLogin(email!!, password!!)
        authListener?.onAuthSuccess(loginResponse)
    }

    fun onResetButtonClick(view: View) {
        authListener?.onAuthStarted()

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.EMPTY_EMAIL))
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

    fun onSignupButtonClick(view: View) {
        authListener?.onAuthStarted()

        // controlla la info aggiunte
        if (fullName.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.EMPTY_NAME))
            return
        }

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.EMPTY_EMAIL))
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.EMPTY_PASSWORD))
            return
        }

        if (password!!.length < 8) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.SHORT_PASSWORD))
            return
        }

        if (passwordConfirm.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.EMPTY_PASSWORD_CONFIRM))
            return
        }

        if (passwordConfirm != password) {
            authListener?.onAuthFailure(AuthResult(AUTH_RESULT.PASSWORD_NOT_MATCH))
            return
        }

        val signupResponse = userRepository.userSignup(fullName!!, email!!, password!!)
        authListener?.onAuthSuccess(signupResponse)
    }

    fun updateUserData() {
        val response = purchaseRepository.updatePurchaseList()
        authListener?.onAuthSuccess(response)
    }

    fun getUserName(): String {
        return userRepository.getUser()!!.fullName!!
    }
}