package com.frafio.myfinance.core.data.manager

import android.util.Log
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.enums.auth.SignupException
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.remote.AuthDataSource
import com.frafio.myfinance.core.data.remote.AuthException
import com.frafio.myfinance.core.data.remote.AuthException.Kind
import com.frafio.myfinance.core.data.repository.ExpensesLocalRepository
import com.frafio.myfinance.core.data.repository.IncomesLocalRepository
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.core.data.storage.ProfileImageStorage
import com.frafio.myfinance.core.di.Dispatcher
import com.frafio.myfinance.core.di.MyFinanceDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val auth: AuthDataSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val expensesLocalRepository: ExpensesLocalRepository,
    private val incomesLocalRepository: IncomesLocalRepository,
    private val profileImageStorage: ProfileImageStorage,
    @Dispatcher(MyFinanceDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        private val TAG = AuthManager::class.java.simpleName
    }

    suspend fun updateFullName(fullName: String): AuthResult = withContext(ioDispatcher) {
        return@withContext try {
            val user = auth.updateDisplayName(fullName)
            userPreferencesRepository.updateUser(user)
            AuthResult(AuthCode.USER_FULL_NAME_UPDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            AuthResult(AuthCode.USER_FULL_NAME_NOT_UPDATED)
        }
    }

    suspend fun isUserLogged(): AuthResult = withContext(ioDispatcher) {
        val user = auth.currentUser
        return@withContext if (user != null) {
            userPreferencesRepository.updateUser(user)
            AuthResult(AuthCode.USER_LOGGED)
        } else {
            AuthResult(AuthCode.USER_NOT_LOGGED)
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): AuthResult = withContext(ioDispatcher) {
        return@withContext try {
            val user = auth.signInWithGoogle(idToken)
            Log.d(TAG, "signInWithCredential:success")
            userPreferencesRepository.updateUser(user)
            AuthResult(AuthCode.LOGIN_SUCCESS)
        } catch (e: Exception) {
            Log.w(TAG, "signInWithCredential:failure", e)
            AuthResult(AuthCode.GOOGLE_LOGIN_FAILURE)
        }
    }

    suspend fun defaultLogin(email: String, password: String): AuthResult = withContext(ioDispatcher) {
        return@withContext try {
            val user = auth.signInWithEmail(email, password)
            userPreferencesRepository.updateUser(user)
            AuthResult(AuthCode.LOGIN_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            when ((e as? AuthException)?.kind) {
                Kind.INVALID_CREDENTIALS -> when (e.errorCode) {
                    SignupException.EXCEPTION_INVALID_EMAIL.value -> AuthResult(AuthCode.INVALID_EMAIL)
                    SignupException.EXCEPTION_WRONG_PASSWORD.value -> AuthResult(AuthCode.WRONG_PASSWORD)
                    else -> AuthResult(AuthCode.LOGIN_FAILURE)
                }
                Kind.INVALID_USER -> when (e.errorCode) {
                    SignupException.EXCEPTION_USER_NOT_FOUND.value -> AuthResult(AuthCode.USER_NOT_FOUND)
                    SignupException.EXCEPTION_USER_DISABLED.value -> AuthResult(AuthCode.USER_DISABLED)
                    else -> AuthResult(AuthCode.LOGIN_FAILURE)
                }
                else -> AuthResult(AuthCode.LOGIN_FAILURE)
            }
        }
    }

    suspend fun resetPassword(email: String): AuthResult = withContext(ioDispatcher) {
        return@withContext try {
            auth.sendPasswordResetEmail(email)
            AuthResult(AuthCode.EMAIL_SENT)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            if ((e as? AuthException)?.kind == Kind.TOO_MANY_REQUESTS) {
                AuthResult(AuthCode.EMAIL_NOT_SENT_TOO_MANY_REQUESTS)
            } else {
                AuthResult(AuthCode.EMAIL_NOT_SENT)
            }
        }
    }

    suspend fun changePassword(newPassword: String, currentPassword: String? = null): AuthResult = withContext(ioDispatcher) {
        val user = auth.currentUser ?: return@withContext AuthResult(AuthCode.USER_NOT_LOGGED)

        return@withContext try {
            if (currentPassword != null) {
                auth.reauthenticate(user.email, currentPassword)
            }
            auth.updatePassword(newPassword)
            userPreferencesRepository.updateUser(auth.currentUser ?: user)
            AuthResult(AuthCode.PASSWORD_UPDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error changing password! ${e.localizedMessage}")
            when ((e as? AuthException)?.kind) {
                Kind.WEAK_PASSWORD -> AuthResult(AuthCode.WEAK_PASSWORD)
                Kind.INVALID_CREDENTIALS -> AuthResult(AuthCode.WRONG_OLD_PASSWORD)
                else -> AuthResult(AuthCode.PASSWORD_NOT_UPDATED)
            }
        }
    }

    suspend fun signup(fullName: String, email: String, password: String): AuthResult = withContext(ioDispatcher) {
        return@withContext try {
            auth.createUser(email, password)

            auth.sendEmailVerification()
            Log.d(TAG, AuthCode.EMAIL_SENT.message)

            val user = auth.updateDisplayName(fullName)

            userPreferencesRepository.updateUser(user)
            AuthResult(AuthCode.SIGNUP_SUCCESS)
        } catch (e: Exception) {
            Log.e(TAG, "Error! ${e.localizedMessage}")
            when ((e as? AuthException)?.kind) {
                Kind.WEAK_PASSWORD -> AuthResult(AuthCode.WEAK_PASSWORD)
                Kind.INVALID_CREDENTIALS -> AuthResult(AuthCode.EMAIL_NOT_WELL_FORMED)
                Kind.USER_COLLISION -> AuthResult(AuthCode.EMAIL_ALREADY_ASSOCIATED)
                else -> AuthResult(AuthCode.SIGNUP_FAILURE)
            }
        }
    }

    suspend fun logout(): AuthResult = withContext(ioDispatcher) {
        auth.signOut()

        expensesLocalRepository.deleteAll()
        incomesLocalRepository.deleteAll()
        profileImageStorage.deleteImage()

        userPreferencesRepository.updateMonthlyBudget(0.0)
        userPreferencesRepository.updateLabels(emptyList())
        userPreferencesRepository.resetSyncTimestamps()
        userPreferencesRepository.clearUserData()

        return@withContext AuthResult(AuthCode.LOGOUT_SUCCESS)
    }
}
