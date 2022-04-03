package com.frafio.myfinance.data.repositories

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.managers.AuthManager
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.User
import com.frafio.myfinance.data.storages.UserStorage

class UserRepository(private val authManager: AuthManager) {

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

    fun getProPic(): String? {
        return UserStorage.user?.photoUrl
    }

    fun updateUserData(): LiveData<AuthResult> {
        return authManager.updateUserData()
    }

    fun isDynamicColorOn(): Boolean {
        return authManager.isDynamicColorOn()
    }
}