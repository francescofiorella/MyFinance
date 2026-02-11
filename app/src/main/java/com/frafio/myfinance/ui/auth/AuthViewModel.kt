package com.frafio.myfinance.ui.auth

import android.app.Application
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
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

    var credentialManager: CredentialManager? = null

    fun onLoginButtonClick() {
        authListener?.onAuthStarted()

        var hasError = false
        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_EMAIL))
            hasError = true
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_PASSWORD))
            hasError = true
        }

        if (!password.isNullOrEmpty() && password!!.length < 8) {
            authListener?.onAuthFailure(AuthResult(AuthCode.SHORT_PASSWORD))
            hasError = true
        }

        if (hasError) return

        val loginResponse = userRepository.userLogin(email!!, password!!)
        authListener?.onAuthSuccess(loginResponse)
    }

    fun resetPassword(email: String) {
        authListener?.onAuthStarted()
        val resetResponse = userRepository.resetPassword(email)
        authListener?.onAuthSuccess(resetResponse)
    }

    fun onGoogleRequest(credential: Credential) {
        authListener?.onAuthStarted()

        val googleResponse = userRepository.userLogin(credential)
        authListener?.onAuthSuccess(googleResponse)
    }

    fun onSignupButtonClick() {
        authListener?.onAuthStarted()

        // check info
        var hasError = false
        if (fullName.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_NAME))
            hasError = true
        }

        if (email.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_EMAIL))
            hasError = true
        }

        if (password.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_PASSWORD))
            hasError = true
        } else if (password!!.length < 8) {
            authListener?.onAuthFailure(AuthResult(AuthCode.SHORT_PASSWORD))
            hasError = true
        }

        if (passwordConfirm.isNullOrEmpty()) {
            authListener?.onAuthFailure(AuthResult(AuthCode.EMPTY_CONFIRM_PASSWORD))
            hasError = true
        } else if (!password.isNullOrEmpty() && passwordConfirm != password) {
            authListener?.onAuthFailure(AuthResult(AuthCode.PASSWORD_NOT_MATCH))
            hasError = true
        }

        if (hasError) return

        val signupResponse = userRepository.userSignup(fullName!!, email!!, password!!)
        authListener?.onAuthSuccess(signupResponse)
    }

    fun getUserName(): String {
        return userRepository.getUser()!!.fullName!!
    }
}