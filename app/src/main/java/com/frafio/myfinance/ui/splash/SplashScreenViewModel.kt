package com.frafio.myfinance.ui.splash

import androidx.lifecycle.ViewModel
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.UserRepository

class SplashScreenViewModel(
    private val userRepository: UserRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    var listener: SplashScreenListener? = null

    fun checkUser() {
        val response = userRepository.isUserLogged()
        listener?.onComplete(response)
    }

    fun updateUserData() {
        val response = purchaseRepository.updatePurchaseList()
        listener?.onComplete(response)
    }
}