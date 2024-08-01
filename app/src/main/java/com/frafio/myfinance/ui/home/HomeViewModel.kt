package com.frafio.myfinance.ui.home

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.repositories.PurchaseRepository
import com.frafio.myfinance.data.repositories.UserRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(
        (application as MyFinanceApplication).authManager
    )
    private val purchaseRepository = PurchaseRepository(
        (application as MyFinanceApplication).purchaseManager
    )
    var listener: HomeListener? = null

    fun checkUser() {
        listener?.onSplashOperationComplete(userRepository.isUserLogged())
    }

    fun getProPic(): String? {
        return userRepository.getProPic()
    }

    fun onLogoutButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        val logoutResponse = userRepository.userLogout()
        listener?.onLogOutSuccess(logoutResponse)
    }

    fun isDynamicColorOn(): Boolean {
        return userRepository.isDynamicColorOn()
    }

    fun updateUserPurchases() {
        listener?.onUserDataUpdated(purchaseRepository.updatePurchaseList())
    }

    fun updateMonthlyBudget() {
        val response = purchaseRepository.getMonthlyBudget()
        listener?.onUserDataUpdated(response)
    }

    fun updateLocalMonthlyBudget() {
        purchaseRepository.updateLocalMonthlyBudget()
    }
}