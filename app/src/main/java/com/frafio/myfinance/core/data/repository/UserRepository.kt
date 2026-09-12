package com.frafio.myfinance.core.data.repository

import android.graphics.Bitmap
import androidx.credentials.Credential
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val profilePicture: Flow<Bitmap?>

    val userData: Flow<User?>

    suspend fun updateFullName(fullName: String): AuthResult

    suspend fun userLogin(email: String, password: String): AuthResult

    suspend fun userLogin(credential: Credential): AuthResult

    suspend fun resetPassword(email: String): AuthResult

    suspend fun changePassword(newPassword: String, currentPassword: String? = null): AuthResult

    suspend fun userSignup(fullName: String, email: String, password: String): AuthResult

    suspend fun userLogout(): AuthResult

    suspend fun isUserLogged(): AuthResult

    suspend fun syncProfilePicture(photoUrl: String?)

    fun getCurrentUser(): User?

    fun isUserLoggedIn(): Boolean
}
