package com.frafio.myfinance.testing.repository

import android.graphics.Bitmap
import androidx.credentials.Credential
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Holds the signed-in [User] as state and answers auth calls with configurable [AuthResult]s.
 * Defaults are the success codes the ViewModels branch on.
 */
class TestUserRepository : UserRepository {

    private val userFlow = MutableStateFlow<User?>(null)
    private val profilePictureFlow = MutableStateFlow<Bitmap?>(null)

    override val userData: Flow<User?> = userFlow
    override val profilePicture: Flow<Bitmap?> = profilePictureFlow

    var updateFullNameResult = AuthResult(AuthCode.USER_FULL_NAME_UPDATED)
    var loginResult = AuthResult(AuthCode.LOGIN_SUCCESS)
    var credentialLoginResult = AuthResult(AuthCode.LOGIN_SUCCESS)
    var resetPasswordResult = AuthResult(AuthCode.EMAIL_SENT)
    var changePasswordResult = AuthResult(AuthCode.PASSWORD_UPDATED)
    var signupResult = AuthResult(AuthCode.SIGNUP_SUCCESS)
    var logoutResult = AuthResult(AuthCode.LOGOUT_SUCCESS)
    var isUserLoggedResult = AuthResult(AuthCode.USER_LOGGED)
    /** When set, [isUserLogged] throws it instead of answering. */
    var isUserLoggedError: Exception? = null
    var loggedIn = true

    val fullNameUpdates = mutableListOf<String>()
    val loginCalls = mutableListOf<Pair<String, String>>()
    val resetPasswordCalls = mutableListOf<String>()
    val changePasswordCalls = mutableListOf<Pair<String, String?>>()
    val signupCalls = mutableListOf<Triple<String, String, String>>()
    val syncedPhotoUrls = mutableListOf<String?>()
    var logoutCount = 0
        private set

    override suspend fun updateFullName(fullName: String): AuthResult {
        fullNameUpdates += fullName
        return updateFullNameResult
    }

    override suspend fun userLogin(email: String, password: String): AuthResult {
        loginCalls += email to password
        return loginResult
    }

    override suspend fun userLogin(credential: Credential): AuthResult = credentialLoginResult

    override suspend fun resetPassword(email: String): AuthResult {
        resetPasswordCalls += email
        return resetPasswordResult
    }

    override suspend fun changePassword(newPassword: String, currentPassword: String?): AuthResult {
        changePasswordCalls += newPassword to currentPassword
        return changePasswordResult
    }

    override suspend fun userSignup(fullName: String, email: String, password: String): AuthResult {
        signupCalls += Triple(fullName, email, password)
        return signupResult
    }

    override suspend fun userLogout(): AuthResult {
        logoutCount++
        if (logoutResult.code == AuthCode.LOGOUT_SUCCESS.code) {
            userFlow.value = null
            profilePictureFlow.value = null
        }
        return logoutResult
    }

    override suspend fun isUserLogged(): AuthResult {
        isUserLoggedError?.let { throw it }
        return isUserLoggedResult
    }

    override suspend fun syncProfilePicture(photoUrl: String?) {
        syncedPhotoUrls += photoUrl
    }

    override fun getCurrentUser(): User? = userFlow.value

    override fun isUserLoggedIn(): Boolean = loggedIn

    /**
     * A test-only API to allow controlling the signed-in user from tests.
     */
    fun setUser(user: User?) {
        userFlow.value = user
    }
}
