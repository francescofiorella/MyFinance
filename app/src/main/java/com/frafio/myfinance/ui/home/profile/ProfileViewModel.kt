package com.frafio.myfinance.ui.home.profile

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.manager.UserStorage
import com.frafio.myfinance.data.repositories.UserRepository

class ProfileViewModel(
    repository: UserRepository
) : ViewModel() {

    val user = repository.getUser()
}