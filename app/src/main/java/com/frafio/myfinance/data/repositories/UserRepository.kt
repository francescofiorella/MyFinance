package com.frafio.myfinance.data.repositories

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.managers.AuthManager
import com.frafio.myfinance.data.managers.PurchaseManager
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.User
import com.frafio.myfinance.data.storage.UserStorage

class UserRepository(private val authManager: AuthManager, private val purchaseManager: PurchaseManager) {

    fun userLogin(email: String, password: String): LiveData<AuthResult> {
        return authManager.defaultLogin(email, password)
    }

    fun userLogin(data: Intent?): LiveData<AuthResult> {
        return authManager.googleLogin(data)
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
        return UserStorage.user
    }

    fun updateUserData(): LiveData<AuthResult> {
        return purchaseManager.updateList()
    }
}