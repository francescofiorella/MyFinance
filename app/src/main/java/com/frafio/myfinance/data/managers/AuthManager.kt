package com.frafio.myfinance.data.managers

import com.frafio.myfinance.data.storage.UserStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {

    companion object{
        const val USER_LOGGED: Int = 1
        const val USER_NOT_LOGGED: Int = 2
    }

    // FirebaseAuth
    private val fAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    // FirebaseUser
    private val fUser: FirebaseUser?
        get() = fAuth.currentUser

    fun isUserLogged(): Int {
        fUser.also {
            return if (it != null) {
                UserStorage.updateUser(it)
                USER_LOGGED
            } else {
                USER_NOT_LOGGED
            }
        }
    }
}