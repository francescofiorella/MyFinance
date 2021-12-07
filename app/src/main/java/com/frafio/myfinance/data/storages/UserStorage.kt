package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.models.User
import com.google.firebase.auth.FirebaseUser

object UserStorage {
    private var privateUser: User? = null
    val user: User?
        get() = privateUser

    fun updateUser(fUser: FirebaseUser) {
        var userPic = ""
        fUser.photoUrl?.let { uri ->
            userPic = uri.toString()
        }
        privateUser = User(fUser.displayName, fUser.email, userPic)
    }

    fun resetUser() {
        privateUser = null
    }
}