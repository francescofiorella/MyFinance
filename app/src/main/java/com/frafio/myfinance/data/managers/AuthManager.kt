package com.frafio.myfinance.data.managers

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.enums.auth.SigninException
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.frafio.myfinance.utils.getSharedCollection
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AuthManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = AuthManager::class.java.simpleName
    }

    // FirebaseFirestore
    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

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
                            SigninException.EXCEPTION_INVALID_EMAIL.value -> response.value =
                                AuthResult(
                                    AuthCode.INVALID_EMAIL
                                )

                            SigninException.EXCEPTION_WRONG_PASSWORD.value -> response.value =
                                AuthResult(
                                    AuthCode.WRONG_PASSWORD
                                )

                            else -> response.value = AuthResult(AuthCode.LOGIN_FAILURE)
                        }
                    }

                    is FirebaseAuthInvalidUserException -> {
                        when (e.errorCode) {
                            SigninException.EXCEPTION_USER_NOT_FOUND.value -> response.value =
                                AuthResult(
                                    AuthCode.USER_NOT_FOUND
                                )

                            SigninException.EXCEPTION_USER_DISABLED.value -> response.value =
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
                    response.value = AuthResult(AuthCode.PROFILE_NOT_UPDATED)
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

        PurchaseStorage.resetPurchaseList()
        UserStorage.resetUser()

        response.value = AuthResult(AuthCode.LOGOUT_SUCCESS)
        return (response)
    }

    fun updateUserData(): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        checkLastYear()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!).collection(getSharedCollection(sharedPreferences))
            .whereEqualTo(DbPurchases.FIELDS.EMAIL.value, UserStorage.user!!.email)
            .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.TYPE.value)
            .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                PurchaseStorage.resetPurchaseList()

                queryDocumentSnapshots.forEach { document ->
                    val purchase = document.toObject(Purchase::class.java)

                    // set id
                    purchase.updateID(document.id)

                    PurchaseStorage.purchaseList.add(purchase)
                }

                response.value = AuthResult(AuthCode.USER_DATA_UPDATED)
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = AuthResult(AuthCode.USER_DATA_NOT_UPDATED)
            }

        return response
    }

    fun checkLastYear() {
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!).collection(DbPurchases.COLLECTIONS.ZERO_UNO.value)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                PurchaseStorage.existLastYear = !queryDocumentSnapshots.isEmpty
            }
    }

    fun getIsUserLogged(): Boolean {
        return UserStorage.user != null
    }
}