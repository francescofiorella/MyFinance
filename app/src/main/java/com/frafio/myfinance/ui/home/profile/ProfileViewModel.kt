package com.frafio.myfinance.ui.home.profile

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.UserRepository

class ProfileViewModel(
    userRepository: UserRepository
) : ViewModel() {
    val user = userRepository.getUser()
}