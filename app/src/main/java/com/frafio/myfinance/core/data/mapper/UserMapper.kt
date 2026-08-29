package com.frafio.myfinance.core.data.mapper

import com.frafio.myfinance.core.data.model.User
import com.google.firebase.auth.FirebaseUser
import java.util.Calendar

fun FirebaseUser.toUser(): User {
    var userPic = ""
    this.photoUrl?.let { uri ->
        userPic = uri.toString().replace("s96-c", "s400-c")
    }
    val providers = this.providerData.map { it.providerId }.toMutableList()
    if (!providers.contains(this.providerId)) {
        providers.add(this.providerId)
    }

    val hasPassword = providers.any { it.contains("password") }
    val isGoogleLinked = providers.any { it.contains("google.com") }

    var provider = User.EMAIL_PROVIDER
    if (isGoogleLinked) {
        provider = User.GOOGLE_PROVIDER
    }
    var day: Int? = null
    var month: Int? = null
    var year: Int? = null
    this.metadata?.let {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = it.creationTimestamp
        day = calendar.get(Calendar.DAY_OF_MONTH)
        month = calendar.get(Calendar.MONTH) + 1
        year = calendar.get(Calendar.YEAR)
    }
    return User(this.displayName, this.email, userPic, null, provider, providers, hasPassword, isGoogleLinked, year, month, day)
}
