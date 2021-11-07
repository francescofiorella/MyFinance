package com.frafio.myfinance.data.storages

import com.frafio.myfinance.data.models.User
import com.google.firebase.auth.FirebaseUser

object UserStorage {
    private var _user: User? = null
    val user: User?
        get() = _user

    private var _isLogged: Boolean = false
    val isLogged: Boolean = _isLogged

    fun updateUser(fUser: FirebaseUser) {
        var userPic = ""
        fUser.photoUrl?.let { uri ->
            userPic = uri.toString()
        }
        _user = User(fUser.displayName, fUser.email, userPic)
        _isLogged = true
    }

    fun resetUser() {
        _user = null
        _isLogged = false
    }
}