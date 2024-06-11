package com.frafio.myfinance.data.managers

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.enums.auth.SignupException
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.models.AuthResult
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.frafio.myfinance.utils.getSharedCategory
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.setSharedCategory
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

        PurchaseStorage.resetPurchaseList()
        UserStorage.resetUser()
        setSharedCategory(sharedPreferences, DbPurchases.CATEGORIES.DEFAULT.value)

        response.value = AuthResult(AuthCode.LOGOUT_SUCCESS)
        return (response)
    }

    fun updateUserData(): LiveData<AuthResult> {
        val response = MutableLiveData<AuthResult>()

        // set the current collection
        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .get().addOnSuccessListener { docSnap ->
                val categories =
                    (docSnap.data?.get(DbPurchases.FIELDS.CATEGORIES.value) as List<*>?)?.map { value ->
                        value.toString()
                    } ?: listOf()
                if (categories.isEmpty()) {
                    setSharedCategory(sharedPreferences, DbPurchases.CATEGORIES.DEFAULT.value)

                    // initialize the categories vector
                    fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                        .document(UserStorage.user!!.email!!)
                        .update(
                            "categories",
                            FieldValue.arrayUnion(DbPurchases.CATEGORIES.DEFAULT.value)
                        )
                        .addOnFailureListener { e ->
                            setSharedCategory(
                                sharedPreferences,
                                DbPurchases.CATEGORIES.DEFAULT.value
                            )
                            val error = "Error! ${e.localizedMessage}"
                            Log.e(TAG, error)

                            response.value = AuthResult(AuthCode.USER_DATA_NOT_UPDATED)
                        }
                } else {
                    val cat = categories.last()
                    setSharedCategory(sharedPreferences, cat)
                }

                fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                    .document(UserStorage.user!!.email!!)
                    .collection(DbPurchases.FIELDS.PAYMENTS.value)
                    .whereEqualTo(
                        DbPurchases.FIELDS.CATEGORY.value,
                        getSharedCategory(sharedPreferences)
                    )
                    .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
                    .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
                    .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
                    .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
                    .get().addOnSuccessListener { queryDocumentSnapshots ->
                        PurchaseStorage.resetPurchaseList()
                        // Create total for the local list
                        var total: Purchase? = null
                        // Used to keep the order
                        var currentPurchases = mutableListOf<Purchase>()

                        queryDocumentSnapshots.forEach { document ->
                            val purchase = document.toObject(Purchase::class.java)
                            if (purchase.type != DbPurchases.TYPES.TOTAL.value) {
                                // set id
                                purchase.updateID(document.id)

                                var todayDate = LocalDate.now()
                                val purchaseDate =
                                    LocalDate.of(purchase.year!!, purchase.month!!, purchase.day!!)
                                var prevDate: LocalDate? = if (total == null)
                                    null
                                else
                                    LocalDate.of(total!!.year!!, total!!.month!!, total!!.day!!)

                                // se è < today and non hai fatto today
                                // quindi se purchase < today and (totale == null or
                                // totale > today)
                                if (prevDate == null &&
                                    ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0
                                ) {
                                    // Aggiungi totali a 0 per ogni giorno tra oggi e purchase
                                    val totToAdd = ChronoUnit.DAYS.between(purchaseDate, todayDate)
                                    for (i in 0..<totToAdd) {
                                        val totId =
                                            "${todayDate.dayOfMonth}_${todayDate.monthValue}_${todayDate.year}"
                                        total = Purchase(
                                            email = UserStorage.user!!.email,
                                            name = DbPurchases.NAMES.TOTAL.value,
                                            price = 0.0,
                                            year = todayDate.year,
                                            month = todayDate.monthValue,
                                            day = todayDate.dayOfMonth,
                                            type = 0,
                                            id = totId,
                                            category = purchase.category
                                        )
                                        PurchaseStorage.purchaseList.add(total!!)
                                        prevDate =
                                            LocalDate.of(
                                                total!!.year!!,
                                                total!!.month!!,
                                                total!!.day!!
                                            )
                                        todayDate = todayDate.minusDays(1)
                                    }
                                    todayDate = LocalDate.now()
                                }

                                var totId = "${purchase.day}_${purchase.month}_${purchase.year}"
                                if (prevDate == null) { // If is the first total
                                    currentPurchases.add(purchase)
                                    total = Purchase(
                                        email = UserStorage.user!!.email,
                                        name = DbPurchases.NAMES.TOTAL.value,
                                        price = if (purchase.type != DbPurchases.TYPES.RENT.value)
                                            purchase.price else 0.0,
                                        year = purchase.year,
                                        month = purchase.month,
                                        day = purchase.day,
                                        type = 0,
                                        id = totId,
                                        category = purchase.category
                                    )
                                } else if (total!!.id == totId) { // If the total should be updated
                                    currentPurchases.add(purchase)
                                    if (purchase.type != DbPurchases.TYPES.RENT.value) {
                                        total!!.price = total!!.price!!.plus(purchase.price ?: 0.0)
                                    }
                                } else { // If we need a new total
                                    // Update the local list with previous day purchases
                                    if (currentPurchases.isNotEmpty()) {
                                        PurchaseStorage.purchaseList.add(total!!)
                                        currentPurchases.forEach { cPurchase ->
                                            PurchaseStorage.purchaseList.add(cPurchase)
                                        }
                                    }
                                    // aggiungi 0 anche se totale - purchase > 1,
                                    // aggiungi uno 0 per ogni differenza tra totale e purchase
                                    val startFromToday =
                                        ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0 &&
                                                ChronoUnit.DAYS.between(todayDate, prevDate) > 0
                                    val totToAdd = if (startFromToday)
                                        ChronoUnit.DAYS.between(purchaseDate, todayDate) + 1
                                    else
                                        ChronoUnit.DAYS.between(purchaseDate, prevDate)
                                    if (ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0 &&
                                        totToAdd > 1
                                    ) {
                                        if (startFromToday) {
                                            prevDate = LocalDate.now().plusDays(1)
                                        }
                                        for (i in 1..<totToAdd) {
                                            prevDate = prevDate!!.minusDays(1)
                                            totId =
                                                "${prevDate.dayOfMonth}_${prevDate.monthValue}_${prevDate.year}"
                                            total = Purchase(
                                                email = UserStorage.user!!.email,
                                                name = DbPurchases.NAMES.TOTAL.value,
                                                price = 0.0,
                                                year = prevDate.year,
                                                month = prevDate.monthValue,
                                                day = prevDate.dayOfMonth,
                                                type = 0,
                                                id = totId,
                                                category = purchase.category
                                            )
                                            PurchaseStorage.purchaseList.add(total!!)
                                        }
                                    }

                                    // Create new total
                                    currentPurchases = mutableListOf()
                                    currentPurchases.add(purchase)
                                    totId = "${purchase.day}_${purchase.month}_${purchase.year}"
                                    total = Purchase(
                                        email = UserStorage.user!!.email,
                                        name = DbPurchases.NAMES.TOTAL.value,
                                        price = if (purchase.type != DbPurchases.TYPES.RENT.value)
                                            purchase.price else 0.0,
                                        year = purchase.year,
                                        month = purchase.month,
                                        day = purchase.day,
                                        type = 0,
                                        id = totId,
                                        category = purchase.category
                                    )
                                }
                            }
                        }

                        response.value = AuthResult(AuthCode.USER_DATA_UPDATED)
                    }.addOnFailureListener { e ->
                        val error = "Error! ${e.localizedMessage}"
                        Log.e(TAG, error)

                        response.value = AuthResult(AuthCode.USER_DATA_NOT_UPDATED)
                    }
            }.addOnFailureListener { e ->
                setSharedCategory(sharedPreferences, DbPurchases.CATEGORIES.DEFAULT.value)
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = AuthResult(AuthCode.USER_DATA_NOT_UPDATED)
            }

        return response
    }

    fun isDynamicColorOn(): Boolean {
        return getSharedDynamicColor(sharedPreferences)
    }
}