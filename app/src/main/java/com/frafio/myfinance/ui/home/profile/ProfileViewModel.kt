package com.frafio.myfinance.ui.home.profile

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.manager.UserManager

class ProfileViewModel : ViewModel() {

    val user = UserManager.getUser()
}