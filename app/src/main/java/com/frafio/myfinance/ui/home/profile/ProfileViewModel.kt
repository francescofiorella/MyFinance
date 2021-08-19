package com.frafio.myfinance.ui.home.profile

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.data.repositories.UserRepository

class ProfileViewModel(
    private val repository: UserRepository
) : ViewModel() {
    val user = repository.getUser()

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"
    var profileListener: ProfileListener? = null

    fun onLogoutButtonClick(view: View) {
        val logoutResponse = repository.userLogout()
        profileListener?.onLogOutSuccess(logoutResponse)
    }
}