package com.frafio.myfinance.data.manager

import android.util.Log
import com.frafio.myfinance.data.enums.auth.AuthCode
import com.frafio.myfinance.data.enums.auth.SignupException
import com.frafio.myfinance.data.model.AuthResult
import com.frafio.myfinance.data.repository.IncomesLocalRepository
import com.frafio.myfinance.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.data.repository.UserPreferencesRepository
import com.frafio.myfinance.data.storage.ProfileImageStorage
import com.frafio.myfinance.data.mapper.toUser
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val expensesLocalRepository: ExpensesLocalRepository,
    private val incomesLocalRepository: IncomesLocalRepository,
    private val profileImageStorage: ProfileImageStorage
) {

    companion object {
        private val TAG = AuthManager::class.java.simpleName
    }

    // FirebaseAuth
    private val fAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    // FirebaseUser
    private val fUser: FirebaseUser?
        get() = fAuth.currentUser

    suspend fun updatePropic(propicUri: String): AuthResult = withContext(Dispatchers.IO) {
        val profileUpdates = userProfileChangeRequest {
            if (propicUri.isNotEmpty()) {
                photoUri = propicUri.toUri()
            }
        }

        return@withContext try {
            fUser!!.updateProfile(profileUpdates).await()
            userPreferencesRepository.updateUser(fUser!!.toUser())
            AuthResult(AuthCode.USER_PROPIC_UPDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            AuthResult(AuthCode.USER_PROPIC_NOT_UPDATED)
        }
    }

    suspend fun updateFullName(fullName: String): AuthResult = withContext(Dispatchers.IO) {
        val profileUpdates = userProfileChangeRequest {
            displayName = fullName
        }

        return@withContext try {
            fUser!!.updateProfile(profileUpdates).await()
            userPreferencesRepository.updateUser(fUser!!.toUser())
            AuthResult(AuthCode.USER_FULL_NAME_UPDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            AuthResult(AuthCode.USER_FULL_NAME_NOT_UPDATED)
        }
    }

    suspend fun isUserLogged(): AuthResult = withContext(Dispatchers.IO) {
        val user = fUser
        return@withContext if (user != null) {
            userPreferencesRepository.updateUser(user.toUser())
            AuthResult(AuthCode.USER_LOGGED)
        } else {
            AuthResult(AuthCode.USER_NOT_LOGGED)
        }
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = fAuth.signInWithCredential(credential).await()
            Log.d(TAG, "signInWithCredential:success")
            userPreferencesRepository.updateUser(authResult.user!!.toUser())
            AuthResult(AuthCode.LOGIN_SUCCESS)
        } catch (e: ApiException) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
        } catch (e: Exception) {
            Log.w(TAG, "signInWithCredential:failure", e)
            AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
        }
    }

    suspend fun defaultLogin(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val authResult = fAuth.signInWithEmailAndPassword(email, password).await()
            userPreferencesRepository.updateUser(authResult.user!!.toUser())
            AuthResult(AuthCode.LOGIN_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    when (e.errorCode) {
                        SignupException.EXCEPTION_INVALID_EMAIL.value -> AuthResult(AuthCode.INVALID_EMAIL)
                        SignupException.EXCEPTION_WRONG_PASSWORD.value -> AuthResult(AuthCode.WRONG_PASSWORD)
                        else -> AuthResult(AuthCode.LOGIN_FAILURE)
                    }
                }
                is FirebaseAuthInvalidUserException -> {
                    when (e.errorCode) {
                        SignupException.EXCEPTION_USER_NOT_FOUND.value -> AuthResult(AuthCode.USER_NOT_FOUND)
                        SignupException.EXCEPTION_USER_DISABLED.value -> AuthResult(AuthCode.USER_DISABLED)
                        else -> AuthResult(AuthCode.LOGIN_FAILURE)
                    }
                }
                else -> AuthResult(AuthCode.LOGIN_FAILURE)
            }
        }
    }

    suspend fun resetPassword(email: String): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            fAuth.sendPasswordResetEmail(email).await()
            AuthResult(AuthCode.EMAIL_SENT)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            if (e is FirebaseTooManyRequestsException) {
                AuthResult(AuthCode.EMAIL_NOT_SENT_TOO_MANY_REQUESTS)
            } else {
                AuthResult(AuthCode.EMAIL_NOT_SENT)
            }
        }
    }

    suspend fun signup(fullName: String, email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val authResult = fAuth.createUserWithEmailAndPassword(email, password).await()
            val fUser = authResult.user

            fUser?.sendEmailVerification()?.await()
            Log.d(TAG, AuthCode.EMAIL_SENT.message)

            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(fullName).build()
            fUser?.updateProfile(profileUpdates)?.await()

            userPreferencesRepository.updateUser(authResult.user!!.toUser())
            AuthResult(AuthCode.SIGNUP_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            when (e) {
                is FirebaseAuthWeakPasswordException -> AuthResult(AuthCode.WEAK_PASSWORD)
                is FirebaseAuthInvalidCredentialsException -> AuthResult(AuthCode.EMAIL_NOT_WELL_FORMED)
                is FirebaseAuthUserCollisionException -> AuthResult(AuthCode.EMAIL_ALREADY_ASSOCIATED)
                else -> AuthResult(AuthCode.SIGNUP_FAILURE)
            }
        }
    }

    suspend fun logout(): AuthResult = withContext(Dispatchers.IO) {
        fAuth.signOut()

        expensesLocalRepository.deleteAll()
        incomesLocalRepository.deleteAll()
        profileImageStorage.deleteImage()
        
        userPreferencesRepository.updateMonthlyBudget(0.0)
        userPreferencesRepository.updateLabels(emptyList())
        userPreferencesRepository.clearUserData()

        return@withContext AuthResult(AuthCode.LOGOUT_SUCCESS)
    }
}
