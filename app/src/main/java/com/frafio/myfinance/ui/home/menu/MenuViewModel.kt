package com.frafio.myfinance.ui.home.menu

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.UserRepository

class MenuViewModel(
    userRepository: UserRepository
) : ViewModel() {
    val proPic: String? = userRepository.getProPic()
}