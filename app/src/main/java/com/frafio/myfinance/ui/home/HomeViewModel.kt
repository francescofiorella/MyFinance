package com.frafio.myfinance.ui.home

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.UserRepository

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel(){
    val proPic: String? = userRepository.getProPic()

    var logoutListener: LogoutListener? = null

    fun onLogoutButtonClick(view: View) {
        val logoutResponse = userRepository.userLogout()
        logoutListener?.onLogOutSuccess(logoutResponse)
    }
}