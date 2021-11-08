package com.frafio.myfinance.ui.home

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.ui.splash.SplashScreenListener

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel(){
    var isLayoutReady: Boolean = false

    var listener: HomeListener? = null

    fun checkUser() {
        listener?.onSplashOperationComplete(userRepository.isUserLogged())
    }

    fun updateUserData() {
        listener?.onSplashOperationComplete(userRepository.updateUserData())
    }

    fun isLogged(): Boolean {
        return userRepository.getIsLogged()
    }

    fun getProPic(): String? {
        return userRepository.getProPic()
    }

    fun logOut() {
        val logoutResponse = userRepository.userLogout()
        listener?.onLogOutSuccess(logoutResponse)
    }
}