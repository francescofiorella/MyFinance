package com.frafio.myfinance.ui.home.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frafio.myfinance.BuildConfig
import com.frafio.myfinance.MyFinanceApplication
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.data.repository.PurchaseRepository
import com.frafio.myfinance.data.repository.UserRepository
import com.google.android.material.color.DynamicColors

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository((application as MyFinanceApplication).authManager)
    private val purchaseRepository =
        PurchaseRepository((application as MyFinanceApplication).purchaseManager)
    var user = userRepository.getUser()
    val googleSignIn = user?.provider == User.GOOGLE_PROVIDER

    var listener: ProfileListener? = null

    fun uploadPropic() {
        listener?.onStarted()
        val propicUri = ""
        val response = userRepository.updateProfile(null, propicUri)
        listener?.onProfileUpdateComplete(response)
    }

    fun editFullName(fullName: String) {
        listener?.onStarted()
        val response = userRepository.updateProfile(fullName, null)
        listener?.onProfileUpdateComplete(response)
    }

    fun updateLocalUser() {
        user = userRepository.getUser()
    }

    val versionName: String = "MyFinance ${BuildConfig.VERSION_NAME}"

    var isDynamicColorAvailable: Boolean = DynamicColors.isDynamicColorAvailable()
    var isSwitchDynamicColorChecked: Boolean = getDynamicColorCheck()

    fun setDynamicColor(active: Boolean) {
        purchaseRepository.setDynamicColorActive(active)
    }

    private fun getDynamicColorCheck(): Boolean {
        return purchaseRepository.getDynamicColorActive()
    }
}