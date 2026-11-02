package com.frafio.myfinance.core.data.repository

import android.graphics.Bitmap
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.enums.db.FirestoreEnums
import com.frafio.myfinance.core.data.manager.AuthManager
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.remote.RemoteDataSource
import com.frafio.myfinance.core.data.storage.ProfileImageStorage
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authManager: AuthManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val profileImageStorage: ProfileImageStorage,
    private val okHttpClient: OkHttpClient,
    private val remoteDataSource: RemoteDataSource
) : UserRepository {

    // Downloads run here: they outlive their caller, such as a login that stops waiting.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // One download at a time: the next sync of the same picture finds it already downloaded.
    private val downloadMutex = Mutex()

    // Known right after a download, while the preferences may still lag behind.
    @Volatile
    private var downloadedPhotoUrl: String? = null

    private var _lastUser: User? = null
    private val _profilePicture = kotlinx.coroutines.flow.MutableStateFlow(profileImageStorage.loadBitmapSync())
    override val profilePicture: Flow<Bitmap?> = _profilePicture.asStateFlow()

    companion object {
        private val TAG = UserRepositoryImpl::class.java.simpleName
        const val PROFILE_PICTURE_TIMEOUT_MS = 3_000L
    }

    override suspend fun updateFullName(fullName: String): AuthResult {
        return authManager.updateFullName(fullName)
    }

    override suspend fun userLogin(email: String, password: String): AuthResult {
        return authManager.defaultLogin(email, password).also { restoreProfilePicture(it) }
    }

    override suspend fun userLogin(credential: Credential): AuthResult {
        // Check if credential is of type Google ID
        return if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            // Sign in to Firebase with using the token
            authManager.firebaseAuthWithGoogle(googleIdTokenCredential.idToken).also { restoreProfilePicture(it) }
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
            AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
        }
    }

    override suspend fun resetPassword(email: String): AuthResult {
        return authManager.resetPassword(email)
    }

    override suspend fun changePassword(newPassword: String, currentPassword: String?): AuthResult {
        return authManager.changePassword(newPassword, currentPassword)
    }

    override suspend fun userSignup(fullName: String, email: String, password: String): AuthResult {
        return authManager.signup(fullName, email, password)
    }

    override suspend fun userLogout(): AuthResult {
        val result = authManager.logout()
        if (result.code == AuthCode.LOGOUT_SUCCESS.code) {
            _profilePicture.value = null
            _lastUser = null
            downloadedPhotoUrl = null
        }
        return result
    }

    override suspend fun isUserLogged(): AuthResult {
        return authManager.isUserLogged()
    }

    override suspend fun syncProfilePicture(photoUrl: String?) {
        val user = userPreferencesRepository.userPreferencesFlow.first().user
        startDownload(user, photoUrl).join()
    }

    private fun startDownload(user: User?, photoUrl: String?): Job = scope.launch {
        downloadMutex.withLock { downloadProfilePicture(user, photoUrl) }
    }

    private suspend fun downloadProfilePicture(user: User?, photoUrl: String?) = withContext(Dispatchers.IO) {
        if (photoUrl.isNullOrEmpty() || photoUrl == downloadedPhotoUrl) return@withContext
        if (user?.photoUrl == photoUrl && !user.localPhotoPath.isNullOrEmpty()) {
            return@withContext
        }

        try {
            val request = Request.Builder().url(photoUrl).build()
            val call = okHttpClient.newCall(request)
            // Cancelling the coroutine aborts the request and the body still being read.
            val cancelWatcher = launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    awaitCancellation()
                } finally {
                    call.cancel()
                }
            }
            try {
                call.execute().use { response ->
                    val inputStream = response.body?.byteStream()
                    if (response.isSuccessful && inputStream != null) {
                        val localPath = profileImageStorage.saveImage(inputStream)
                        if (localPath != null && user != null) {
                            userPreferencesRepository.updateUser(user.copy(localPhotoPath = localPath))
                            _profilePicture.value = profileImageStorage.loadBitmap()
                            downloadedPhotoUrl = photoUrl
                        }
                    }
                }
            } finally {
                cancelWatcher.cancel()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile picture: ${e.localizedMessage}")
        }
    }

    // Home's welcome snackbar shows the picture, so it is restored before the login completes.
    private suspend fun restoreProfilePicture(loginResult: AuthResult) {
        if (loginResult.code != AuthCode.LOGIN_SUCCESS.code) return
        val user = authManager.getCurrentUser() ?: return

        val fields = try {
            withTimeoutOrNull(PROFILE_PICTURE_TIMEOUT_MS) { remoteDataSource.getUserFields(user.email) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error reading the profile picture choice: ${e.localizedMessage}")
            null
        } ?: return

        val choice = fields[FirestoreEnums.FIELDS.PRO_PIC_CHOICE.value] as? String
        if (choice == null) {
            userPreferencesRepository.clearProPicChoice()
            return
        }
        userPreferencesRepository.updateProPicChoice(choice)
        if (choice != FirestoreEnums.PRO_PIC_TYPES.GOOGLE.value) return

        // Past the timeout the download goes on in the background.
        withTimeoutOrNull(PROFILE_PICTURE_TIMEOUT_MS) { startDownload(user, user.photoUrl).join() }
    }

    override val userData: Flow<User?> = userPreferencesRepository.userPreferencesFlow
        .map { it.user }
        .onEach { _lastUser = it }

    override fun getCurrentUser(): User? {
        return _lastUser ?: runBlocking {
            userPreferencesRepository.userPreferencesFlow.first().user
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return authManager.isUserLoggedIn()
    }
}
