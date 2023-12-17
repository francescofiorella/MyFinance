package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.models.User
import com.google.firebase.auth.FirebaseUser
import java.util.Calendar


object UserStorage {
    private var privateUser: User? = null
    val user: User?
        get() = privateUser

    fun updateUser(fUser: FirebaseUser) {
        var userPic = ""
        fUser.providerId
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
}