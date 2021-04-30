package com.frafio.myfinance.data.repositories

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.ui.auth.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class UserRepository {

    companion object {
        private val TAG = UserRepository::class.java.simpleName
    }

    fun userLogin(email: String, password: String) : LiveData<Any> {
        val response = MutableLiveData<Any>()

        // authenticate the user
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener {
            response.value = 1
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error! ${e.localizedMessage}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    val errorCode = e.errorCode
                    if (errorCode == "ERROR_INVALID_EMAIL") {
                        response.value = 2
                    } else if (errorCode == "ERROR_WRONG_PASSWORD") {
                        response.value = 3
                    } else {
                        response.value = "Accesso fallito!"
                    }
                }
                is FirebaseAuthInvalidUserException -> {
                    val errorCode = e.errorCode
                    if (errorCode == "ERROR_USER_NOT_FOUND") {
                        response.value = 4
                    } else if (errorCode == "ERROR_USER_DISABLED") {
                        response.value = "Il tuo account è stato disabilitato!"
                    } else {
                        response.value = "Accesso fallito!"
                    }
                }
                else -> {
                    response.value = "Accesso fallito!"
                }
            }
        }
        return response
    }

    fun resetPassword(email: String) : LiveData<Any> {
        val response = MutableLiveData<Any>()

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                response.value = "Email inviata. Controlla la tua posta!"
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                if (e is FirebaseTooManyRequestsException) {
                    response.value = "Email non inviata! Sono state effettuate troppe richieste."
                } else {
                    response.value = "Errore! Email non inviata."
                }
            }

        return response
    }

    fun userLogin(data: Intent?) : LiveData<Any> {
        val response = MutableLiveData<Any>()
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
                response.value = if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    1
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "Error! ${task.exception?.localizedMessage}")
                    "Accesso con Google fallito."
                }
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Error! ${e.localizedMessage}")
            response.value = "Accesso con Google fallito!"
        }

        return response
    }
}