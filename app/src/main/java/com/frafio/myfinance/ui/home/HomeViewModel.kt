package com.frafio.myfinance.ui.home

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.ui.splash.SplashScreenListener

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel(){
    val proPic: String? = userRepository.getProPic()
    var isReady: Boolean = false
    var isLoginRequired: Boolean = false

    var listener: HomeListener? = null

    fun onLogoutButtonClick(view: View) {
        val logoutResponse = userRepository.userLogout()
        listener?.onLogOutSuccess(logoutResponse)
    }

    fun checkUser() {
        listener?.onSplashOperationComplete(userRepository.isUserLogged())
    }

    fun updateUserData() {
        val response = userRepository.updateUserData()
        listener?.onSplashOperationComplete(response)
    }
}