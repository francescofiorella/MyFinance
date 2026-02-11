package com.frafio.myfinance.data.repository

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.manager.AuthManager
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.data.storage.MyFinanceStorage
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL

class UserRepository(private val authManager: AuthManager) {

    companion object {
        private val TAG = UserRepository::class.java.simpleName
    }

    fun updateProfile(fullName: String?, propicUri: String?): LiveData<AuthResult> {
        return authManager.updateUserProfile(fullName, propicUri)
    }

    fun userLogin(email: String, password: String): LiveData<AuthResult> {
        return authManager.defaultLogin(email, password)
    }

    fun userLogin(credential: Credential): LiveData<AuthResult> {
        val result: LiveData<AuthResult>

        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            result = authManager.firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
            result = MutableLiveData<AuthResult>()
            result.value = AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
        }

        return result
    }

    fun resetPassword(email: String): LiveData<AuthResult> {
        return authManager.resetPassword(email)
    }

    fun userSignup(fullName: String, email: String, password: String): LiveData<AuthResult> {
        return authManager.signup(fullName, email, password)
    }

    fun userLogout(): LiveData<AuthResult> {
        return authManager.logout()
    }

    fun isUserLogged(): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        response.value = authManager.isUserLogged()
        return response
    }

    fun getUser(): User? {
        return MyFinanceStorage.user
    }

    fun getProPic(): String? {
        return MyFinanceStorage.user?.photoUrl
    }

    fun isDynamicColorOn(): Boolean {
        return authManager.isDynamicColorOn()
    }
}