package com.frafio.myfinance.testing.remote

import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.remote.AuthDataSource
import com.frafio.myfinance.core.data.remote.AuthException
import com.frafio.myfinance.testing.data.testUser

/**
 * In-memory identity provider. Each method has a `*Failure` slot: set an [AuthException] there and
 * the call throws it instead of succeeding. Successful sign-ins and sign-ups make [currentUser]
 * the user for that email.
 */
class TestAuthDataSource : AuthDataSource {

    override var currentUser: User? = null

    var signInFailure: AuthException? = null
    var googleFailure: AuthException? = null
    var createUserFailure: AuthException? = null
    var verificationFailure: AuthException? = null
    var displayNameFailure: AuthException? = null
    var reauthFailure: AuthException? = null
    var updatePasswordFailure: AuthException? = null
    var resetFailure: AuthException? = null

    val signIns = mutableListOf<Pair<String, String>>()
    val googleSignIns = mutableListOf<String>()
    val createdUsers = mutableListOf<Pair<String, String>>()
    var verificationsSent = 0
        private set
    val displayNames = mutableListOf<String>()
    val reauthentications = mutableListOf<Pair<String, String>>()
    val passwordUpdates = mutableListOf<String>()
    val resetEmails = mutableListOf<String>()
    var signOutCount = 0
        private set

    override suspend fun signInWithEmail(email: String, password: String): User {
        signInFailure?.let { throw it }
        signIns += email to password
        return testUser(email = email).also { currentUser = it }
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        googleFailure?.let { throw it }
        googleSignIns += idToken
        return testUser(email = "google@example.com", provider = User.GOOGLE_PROVIDER, isGoogleLinked = true, hasPassword = false)
            .also { currentUser = it }
    }

    override suspend fun createUser(email: String, password: String): User {
        createUserFailure?.let { throw it }
        createdUsers += email to password
        return testUser(email = email, fullName = null).also { currentUser = it }
    }

    override suspend fun sendEmailVerification() {
        verificationFailure?.let { throw it }
        if (currentUser != null) verificationsSent++
    }

    override suspend fun updateDisplayName(fullName: String): User {
        displayNameFailure?.let { throw it }
        val user = currentUser ?: throw AuthException(AuthException.Kind.INVALID_USER)
        displayNames += fullName
        return user.copy(fullName = fullName).also { currentUser = it }
    }

    override suspend fun reauthenticate(email: String, password: String) {
        reauthFailure?.let { throw it }
        reauthentications += email to password
    }

    override suspend fun updatePassword(newPassword: String) {
        updatePasswordFailure?.let { throw it }
        passwordUpdates += newPassword
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        resetFailure?.let { throw it }
        resetEmails += email
    }

    override fun signOut() {
        signOutCount++
        currentUser = null
    }
}
