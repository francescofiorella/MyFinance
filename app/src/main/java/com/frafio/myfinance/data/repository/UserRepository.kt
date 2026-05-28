package com.frafio.myfinance.data.repository

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.manager.AuthManager
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.model.User
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val authManager: AuthManager,
    userPreferencesRepository: UserPreferencesRepository
) {

    companion object {
        private val TAG = UserRepository::class.java.simpleName
    }

    suspend fun updatePropic(propicUri: String): AuthResult {
        return authManager.updatePropic(propicUri)
    }

    suspend fun updateFullName(fullName: String): AuthResult {
        return authManager.updateFullName(fullName)
    }

    suspend fun userLogin(email: String, password: String): AuthResult {
        return authManager.defaultLogin(email, password)
    }

    suspend fun userLogin(credential: Credential): AuthResult {
        // Check if credential is of type Google ID
        return if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            authManager.firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
            AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
        }
    }

    suspend fun resetPassword(email: String): AuthResult {
        return authManager.resetPassword(email)
    }

    suspend fun userSignup(fullName: String, email: String, password: String): AuthResult {
        return authManager.signup(fullName, email, password)
    }

    suspend fun userLogout(): AuthResult {
        return authManager.logout()
    }

    suspend fun isUserLogged(): AuthResult {
        return authManager.isUserLogged()
    }

    val userData: Flow<User?> = userPreferencesRepository.userPreferencesFlow.map { it.user }
}
