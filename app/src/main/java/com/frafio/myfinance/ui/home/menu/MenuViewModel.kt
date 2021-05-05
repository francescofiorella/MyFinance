package com.frafio.myfinance.ui.home.menu

import android.view.View
import androidx.lifecycle.ViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.data.repositories.UserRepository
import com.frafio.myfinance.ui.auth.AuthListener

class MenuViewModel(
    private val repository: UserRepository
) : ViewModel() {

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"
    var authListener: AuthListener? = null

    fun onLogoutButtonClick(view: View) {
        authListener?.onAuthStarted()
        val logoutResponse = repository.userLogout()
        authListener?.onAuthSuccess(logoutResponse)
    }
}