package com.frafio.myfinance.ui.home.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repositories.UserRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository((application as MyFinanceApplication).authManager)
    val user = userRepository.getUser()
}