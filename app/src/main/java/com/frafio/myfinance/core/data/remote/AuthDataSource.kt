package com.frafio.myfinance.core.data.remote

import com.frafio.myfinance.core.data.model.User

/**
 * A failed auth call, already classified. [errorCode] carries the provider's code string
 * (see `SignupException`) for the kinds that distinguish sub-cases.
 */
class AuthException(
    val kind: Kind,
    val errorCode: String? = null,
    cause: Throwable? = null,
) : Exception(cause?.message, cause) {
    enum class Kind { INVALID_CREDENTIALS, INVALID_USER, USER_COLLISION, WEAK_PASSWORD, TOO_MANY_REQUESTS, GOOGLE_SIGN_IN, OTHER }
}

/**
 * Everything `AuthManager` needs from the identity provider, in plain types. Every method throws
 * [AuthException] on failure. [FirebaseAuthDataSource] is the only implementation that names
 * Firebase; tests use `TestAuthDataSource`.
 */
interface AuthDataSource {

    val currentUser: User?

    suspend fun signInWithEmail(email: String, password: String): User

    suspend fun signInWithGoogle(idToken: String): User

    suspend fun createUser(email: String, password: String): User

    /** Sends the verification mail to the signed-in user; a no-op when nobody is signed in. */
    suspend fun sendEmailVerification()

    /** Updates the signed-in user's display name and returns the updated user. */
    suspend fun updateDisplayName(fullName: String): User

    suspend fun reauthenticate(email: String, password: String)

    suspend fun updatePassword(newPassword: String)

    suspend fun sendPasswordResetEmail(email: String)

    fun signOut()
}
