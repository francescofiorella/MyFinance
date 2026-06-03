package com.frafio.myfinance.data.repository

import android.graphics.Bitmap
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.manager.AuthManager
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.model.User
import com.frafio.myfinance.data.storage.ProfileImageStorage
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val authManager: AuthManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val profileImageStorage: ProfileImageStorage,
    private val okHttpClient: OkHttpClient
) {

    private var _lastUser: User? = null
    private val _profilePicture = kotlinx.coroutines.flow.MutableStateFlow(profileImageStorage.loadBitmapSync())
    val profilePicture: Flow<Bitmap?> = _profilePicture.asStateFlow()

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
        val result = authManager.logout()
        if (result.code == AuthCode.LOGOUT_SUCCESS.code) {
            _profilePicture.value = null
            _lastUser = null
        }
        return result
    }

    suspend fun isUserLogged(): AuthResult {
        return authManager.isUserLogged()
    }

    suspend fun syncProfilePicture(photoUrl: String?) = withContext(Dispatchers.IO) {
        if (photoUrl.isNullOrEmpty()) return@withContext

        val user = userPreferencesRepository.userPreferencesFlow.first().user
        if (user?.photoUrl == photoUrl && !user.localPhotoPath.isNullOrEmpty()) {
            return@withContext
        }

        try {
            val request = Request.Builder().url(photoUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val inputStream = response.body?.byteStream()
                if (inputStream != null) {
                    val localPath = profileImageStorage.saveImage(inputStream)
                    if (localPath != null && user != null) {
                        userPreferencesRepository.updateUser(user.copy(localPhotoPath = localPath))
                        _profilePicture.value = profileImageStorage.loadBitmap()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile picture: ${e.localizedMessage}")
        }
    }

    val userData: Flow<User?> = userPreferencesRepository.userPreferencesFlow
        .map { it.user }
        .onEach { _lastUser = it }

    fun getCurrentUser(): User? {
        return _lastUser ?: runBlocking {
            userPreferencesRepository.userPreferencesFlow.first().user
        }
    }
}
