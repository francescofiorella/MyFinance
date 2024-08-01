package com.frafio.myfinance.data.managers

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.enums.auth.SignupException
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.repositories.LocalPurchaseRepository
import com.frafio.myfinance.data.storages.IncomeStorage
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.setSharedMonthlyBudget
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest

class AuthManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = AuthManager::class.java.simpleName
    }

    // FirebaseAuth
    private val fAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    // FirebaseUser
    private val fUser: FirebaseUser?
        get() = fAuth.currentUser

    private val localPurchaseRepository = LocalPurchaseRepository()

    fun updateUserProfile(fullName: String?, propicUri: String?): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()
        val profileUpdates = userProfileChangeRequest {
            fullName?.let {
                displayName = it
            }
            propicUri?.let {
                if (it.isNotEmpty()) {
                    photoUri = Uri.parse(it)
                }
            }
        }

        fUser!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                response.value = if (task.isSuccessful) {
                    UserStorage.updateUser(fUser!!)
                    AuthResult(AuthCode.USER_DATA_UPDATED)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(TAG, "Error! ${task.exception?.localizedMessage}")
                    AuthResult(AuthCode.USER_DATA_NOT_UPDATED)
                }
            }
        return response
    }

    fun isUserLogged(): AuthResult {
        fUser.also {
            return if (it != null) {
                UserStorage.updateUser(it)
                AuthResult(AuthCode.USER_LOGGED)
            } else {
                AuthResult(AuthCode.USER_NOT_LOGGED)
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
                        AuthResult(AuthCode.LOGIN_SUCCESS)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.e(TAG, "Error! ${authTask.exception?.localizedMessage}")
                        AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
                    }
                }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Error! ${e.localizedMessage}")
            response.value = AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
        }

        return response
    }

    fun defaultLogin(email: String, password: String): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        // authenticate the user
        fAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                UserStorage.updateUser(authResult.user!!)
                response.value = AuthResult(AuthCode.LOGIN_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        when (e.errorCode) {
                            SignupException.EXCEPTION_INVALID_EMAIL.value -> response.value =
                                AuthResult(
                                    AuthCode.INVALID_EMAIL
                                )

                            SignupException.EXCEPTION_WRONG_PASSWORD.value -> response.value =
                                AuthResult(
                                    AuthCode.WRONG_PASSWORD
                                )

                            else -> response.value = AuthResult(AuthCode.LOGIN_FAILURE)
                        }
                    }

                    is FirebaseAuthInvalidUserException -> {
                        when (e.errorCode) {
                            SignupException.EXCEPTION_USER_NOT_FOUND.value -> response.value =
                                AuthResult(
                                    AuthCode.USER_NOT_FOUND
                                )

                            SignupException.EXCEPTION_USER_DISABLED.value -> response.value =
                                AuthResult(
                                    AuthCode.USER_DISABLED
                                )

                            else -> response.value = AuthResult(AuthCode.LOGIN_FAILURE)
                        }
                    }

                    else -> response.value = AuthResult(AuthCode.LOGIN_FAILURE)
                }
            }

        return response
    }

    fun resetPassword(email: String): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        fAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                response.value = AuthResult(AuthCode.EMAIL_SENT)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                if (e is FirebaseTooManyRequestsException) {
                    response.value = AuthResult(AuthCode.EMAIL_NOT_SENT_TOO_MANY_REQUESTS)
                } else {
                    response.value = AuthResult(AuthCode.EMAIL_NOT_SENT)
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
                    Log.d(TAG, AuthCode.EMAIL_SENT.message)
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                }

                val profileUpdates =
                    UserProfileChangeRequest.Builder().setDisplayName(fullName).build()

                fUser?.updateProfile(profileUpdates)?.addOnSuccessListener {
                    UserStorage.updateUser(authResult.user!!)
                    response.value = AuthResult(AuthCode.SIGNUP_SUCCESS)
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = AuthResult(AuthCode.SIGNUP_PROFILE_NOT_UPDATED)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                when (e) {
                    is FirebaseAuthWeakPasswordException ->
                        response.value = AuthResult(AuthCode.WEAK_PASSWORD)

                    is FirebaseAuthInvalidCredentialsException ->
                        response.value = AuthResult(AuthCode.EMAIL_NOT_WELL_FORMED)

                    is FirebaseAuthUserCollisionException ->
                        response.value = AuthResult(AuthCode.EMAIL_ALREADY_ASSOCIATED)

                    else -> response.value = AuthResult(AuthCode.SIGNUP_FAILURE)
                }
            }

        return response
    }

    fun logout(): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        fAuth.signOut()

        localPurchaseRepository.deleteAll()
        IncomeStorage.resetIncomeList()
        setSharedMonthlyBudget(sharedPreferences, 0.0)
        PurchaseStorage.resetBudget()
        UserStorage.resetUser()

        response.value = AuthResult(AuthCode.LOGOUT_SUCCESS)
        return (response)
    }

    fun isDynamicColorOn(): Boolean {
        return getSharedDynamicColor(sharedPreferences)
    }
}