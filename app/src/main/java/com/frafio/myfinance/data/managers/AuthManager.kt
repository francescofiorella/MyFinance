package com.frafio.myfinance.data.managers

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.AUTH_RESULT
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.storage.PurchaseStorage
import com.frafio.myfinance.data.storage.UserStorage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

class AuthManager {

    companion object{
        private val TAG = AuthManager::class.java.simpleName

        // Firebase Errors
        private const val ERROR_INVALID_EMAIL: String = "ERROR_INVALID_EMAIL"
        private const val ERROR_WRONG_PASSWORD: String = "ERROR_WRONG_PASSWORD"
        private const val ERROR_USER_NOT_FOUND: String = "ERROR_USER_NOT_FOUND"
        private const val ERROR_USER_DISABLED: String = "ERROR_USER_DISABLED"
    }

    // FirebaseAuth
    private val fAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    // FirebaseUser
    private val fUser: FirebaseUser?
        get() = fAuth.currentUser

    fun isUserLogged(): AuthResult {
        fUser.also {
            return if (it != null) {
                UserStorage.updateUser(it)
                AuthResult(AUTH_RESULT.USER_LOGGED)
            } else {
                AuthResult(AUTH_RESULT.USER_NOT_LOGGED)
            }
        }
    }

    fun googleLogin(data: Intent?): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            // Google Sign In was successful, authenticate with Firebase
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            fAuth.signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    response.value = if (authTask.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        UserStorage.updateUser(authTask.result!!.user!!)
                        AuthResult(AUTH_RESULT.LOGIN_SUCCESS)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "Error! ${authTask.exception?.localizedMessage}")
                        AuthResult(AUTH_RESULT.GOOGLE_LOGIN_FAILURE)
                    }
                }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Error! ${e.localizedMessage}")
            response.value = AuthResult(AUTH_RESULT.GOOGLE_LOGIN_FAILURE)
        }

        return response
    }

    fun defaultLogin(email: String, password: String): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        // authenticate the user
        fAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                UserStorage.updateUser(authResult.user!!)
                response.value = AuthResult(AUTH_RESULT.LOGIN_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        when (e.errorCode) {
                            ERROR_INVALID_EMAIL -> response.value = AuthResult(AUTH_RESULT.INVALID_EMAIL)

                            ERROR_WRONG_PASSWORD -> response.value = AuthResult(AUTH_RESULT.WRONG_PASSWORD)

                            else -> response.value = AuthResult(AUTH_RESULT.LOGIN_FAILURE)
                        }
                    }

                    is FirebaseAuthInvalidUserException -> {
                        when (e.errorCode) {
                            ERROR_USER_NOT_FOUND -> response.value = AuthResult(AUTH_RESULT.USER_NOT_FOUND)

                            ERROR_USER_DISABLED -> response.value = AuthResult(AUTH_RESULT.USER_DISABLED)

                            else -> response.value = AuthResult(AUTH_RESULT.LOGIN_FAILURE)
                        }
                    }

                    else -> response.value = AuthResult(AUTH_RESULT.LOGIN_FAILURE)
                }
            }

        return response
    }

    fun resetPassword(email: String): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        fAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                response.value = AuthResult(AUTH_RESULT.EMAIL_SENT)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                if (e is FirebaseTooManyRequestsException) {
                    response.value = AuthResult(AUTH_RESULT.EMAIL_NOT_SENT_TOO_MANY_REQUESTS)
                } else {
                    response.value = AuthResult(AUTH_RESULT.EMAIL_NOT_SENT)
                }
            }

        return response
    }

    fun signup(fullName: String, email: String, password: String): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        fAuth.createUserWithEmailAndPassword(email, password)
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
                    response.value = AuthResult(AUTH_RESULT.SIGNUP_SUCCESS)
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = AuthResult(AUTH_RESULT.PROFILE_NOT_UPDATED)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                when (e) {
                    is FirebaseAuthWeakPasswordException ->
                        response.value = AuthResult(AUTH_RESULT.WEAK_PASSWORD)

                    is FirebaseAuthInvalidCredentialsException ->
                        response.value = AuthResult(AUTH_RESULT.EMAIL_NOT_WELL_FORMED)

                    is FirebaseAuthUserCollisionException ->
                        response.value = AuthResult(AUTH_RESULT.EMAIL_ALREADY_ASSOCIATED)

                    else -> response.value = AuthResult(AUTH_RESULT.SIGNUP_FAILURE)
                }
            }

        return response
    }

    fun logout(): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        fAuth.signOut()

        PurchaseStorage.resetPurchaseList()
        UserStorage.resetUser()

        response.value = AuthResult(AUTH_RESULT.LOGOUT_SUCCESS)
        return (response)
    }
}