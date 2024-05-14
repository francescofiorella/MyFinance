package com.frafio.myfinance.ui.home.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.models.User
import com.frafio.myfinance.data.repositories.UserRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository((application as MyFinanceApplication).authManager)
    var user = userRepository.getUser()
    val googleSignIn = user?.provider == User.GOOGLE_PROVIDER

    var listener: ProfileListener? = null

    fun uploadPropic() {
        listener?.onStarted()
        val propicUri = ""
        val response = userRepository.updateProfile(null, propicUri)
        listener?.onProfileUpdateComplete(response)
    }

    fun editFullName(fullName: String) {
        listener?.onStarted()
        val response = userRepository.updateProfile(fullName, null)
        listener?.onProfileUpdateComplete(response)
    }

    fun updateLocalUser() {
        user = userRepository.getUser()
    }
}