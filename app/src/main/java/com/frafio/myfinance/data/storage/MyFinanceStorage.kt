package com.frafio.myfinance.data.storage

import com.frafio.myfinance.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

object MyFinanceStorage {
    private var privateUser: User? = null
    val user: User?
        get() = privateUser

    private val _monthlyBudget = MutableStateFlow(0.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _labels = MutableStateFlow<List<String>>(emptyList())
    val labels: StateFlow<List<String>> = _labels.asStateFlow()

    fun updateUser(fUser: FirebaseUser) {
        var userPic = ""
        fUser.photoUrl?.let { uri ->
            userPic = uri.toString().replace("s96-c", "s400-c")
        }
        var provider = User.EMAIL_PROVIDER
        for (user in fUser.providerData) {
            if (user.providerId.contains("google.com"))
                provider = User.GOOGLE_PROVIDER
        }
        var day: Int? = null
        var month: Int? = null
        var year: Int? = null
        fUser.metadata?.let {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it.creationTimestamp
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH) + 1
            year = calendar.get(Calendar.YEAR)
        }
        privateUser = User(fUser.displayName, fUser.email, userPic, provider, year, month, day)
    }

    fun resetUser() {
        privateUser = null
    }

    fun resetBudget() {
        _monthlyBudget.value = 0.0
    }

    fun updateBudget(value: Double) {
        _monthlyBudget.value = value
    }

    fun resetLabels() {
        _labels.value = emptyList()
    }

    fun updateLabels(value: List<String>) {
        _labels.value = value
    }
}
