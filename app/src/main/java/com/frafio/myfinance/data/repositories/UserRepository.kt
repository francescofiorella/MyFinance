package com.frafio.myfinance.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class UserRepository {

    companion object {
        private val TAG = UserRepository::class.java.simpleName
    }

    fun userLogin(email: String, password: String) : LiveData<Int> {

        val loginResponse = MutableLiveData<Int>()

        // authenticate the user
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
            loginResponse.value = 1
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error! ${e.localizedMessage}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    val errorCode = e.errorCode
                    if (errorCode == "ERROR_INVALID_EMAIL") {
                        loginResponse.value = 2
                    } else if (errorCode == "ERROR_WRONG_PASSWORD") {
                        loginResponse.value = 3
                    }
                }
                is FirebaseAuthInvalidUserException -> {
                    val errorCode = e.errorCode
                    if (errorCode == "ERROR_USER_NOT_FOUND") {
                        loginResponse.value = 4
                    } else if (errorCode == "ERROR_USER_DISABLED") {
                        loginResponse.value = 5
                    } else {
                        loginResponse.value = 6
                    }
                }
                else -> {
                    loginResponse.value = 6
                }
            }
        }
        return loginResponse
    }
}