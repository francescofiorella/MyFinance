package com.frafio.myfinance.data.manager

import com.frafio.myfinance.data.models.User
import com.google.firebase.auth.FirebaseUser

object UserStorage {

    private var user: User? = null

    fun updateUser(fUser: FirebaseUser) {
        var userPic = ""
        fUser.photoUrl?.let { uri ->
            userPic = uri.toString()
        }
        user = User(fUser.displayName, fUser.email, userPic)
    }

    fun getUser(): User? {
        return user
    }

    fun resetUser() {
        user = null
    }
}