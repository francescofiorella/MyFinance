package com.frafio.myfinance.ui.home

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.UserRepository

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    var listener: HomeListener? = null

    fun checkUser() {
        listener?.onSplashOperationComplete(userRepository.isUserLogged())
    }

    fun updateUserData() {
        listener?.onSplashOperationComplete(userRepository.updateUserData())
    }

    fun getProPic(): String? {
        return userRepository.getProPic()
    }

    fun onLogoutButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val logoutResponse = userRepository.userLogout()
        listener?.onLogOutSuccess(logoutResponse)
    }

    fun isDynamicColorOn(): Boolean {
        return userRepository.isDynamicColorOn()
    }
}