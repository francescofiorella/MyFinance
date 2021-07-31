package com.frafio.myfinance.data.repositories

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.managers.AuthManager
import com.frafio.myfinance.data.storage.PurchaseStorage
import com.frafio.myfinance.data.storage.UserStorage
import com.frafio.myfinance.data.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

class UserRepository {

    private val authManager: AuthManager = AuthManager()

    companion object {
        private val TAG = UserRepository::class.java.simpleName
    }

    fun userLogin(email: String, password: String): LiveData<Any> {
        val response = MutableLiveData<Any>()

        // authenticate the user
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                UserStorage.updateUser(authResult.user!!)
                response.value = 1
            }.addOnFailureListener { e ->
            Log.e(TAG, "Error! ${e.localizedMessage}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    when (e.errorCode) {
                        "ERROR_INVALID_EMAIL" -> response.value = 2
                        "ERROR_WRONG_PASSWORD" -> response.value = 3
                        else -> response.value = "Accesso fallito!"
                    }
                }
                is FirebaseAuthInvalidUserException -> {
                    when (e.errorCode) {
                        "ERROR_USER_NOT_FOUND" -> response.value = 4
                        "ERROR_USER_DISABLED" -> response.value =
                            "Il tuo account è stato disabilitato!"
                        else -> response.value = "Accesso fallito!"
                    }
                }
                else -> {
                    response.value = "Accesso fallito!"
                }
            }
        }
        return response
    }

    fun userLogin(data: Intent?): LiveData<Any> {
        val response = MutableLiveData<Any>()
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    response.value = if (authTask.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        UserStorage.updateUser(authTask.result!!.user!!)
                        1
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "Error! ${authTask.exception?.localizedMessage}")
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

    fun resetPassword(email: String): LiveData<Any> {
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

    fun userSignup(fullName: String, email: String, password: String): LiveData<Any> {
        val response = MutableLiveData<Any>()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // verify the email
                val fUser = authResult.user
                fUser?.sendEmailVerification()?.addOnSuccessListener {
                    Log.d(TAG, "Email di verifica inviata!")
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                }

                val profileUpdates =
                    UserProfileChangeRequest.Builder().setDisplayName(fullName).build()
                fUser?.updateProfile(profileUpdates)?.addOnSuccessListener {
                    UserStorage.updateUser(authResult.user!!)
                    response.value = 1
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = "Registrazione non avvenuta correttamente!"
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                when (e) {
                    is FirebaseAuthWeakPasswordException ->
                        response.value = 2
                    is FirebaseAuthInvalidCredentialsException ->
                        response.value = 3
                    is FirebaseAuthUserCollisionException ->
                        response.value = 4
                    else -> response.value = "Registrazione fallita."
                }
            }

        return response
    }

    fun userLogout(): LiveData<Any> {
        val response = MutableLiveData<Any>()

        FirebaseAuth.getInstance().signOut()

        PurchaseStorage.resetPurchaseList()
        UserStorage.resetUser()

        response.value = 1
        return (response)
    }

    fun isUserLogged(): LiveData<Any> {
        val response = MutableLiveData<Any>()

        response.value = authManager.isUserLogged()
        return response
    }

    fun getUser(): User? {
        return UserStorage.user
    }
}