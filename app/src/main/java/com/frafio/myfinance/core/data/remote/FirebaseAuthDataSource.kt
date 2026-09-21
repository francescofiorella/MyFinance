package com.frafio.myfinance.core.data.remote

import com.frafio.myfinance.core.data.mapper.toUser
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.remote.AuthException.Kind
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor() : AuthDataSource {

    private val fAuth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val signedInUser: FirebaseUser
        get() = fAuth.currentUser ?: throw AuthException(Kind.INVALID_USER)

    override val currentUser: User?
        get() = fAuth.currentUser?.toUser()

    override suspend fun signInWithEmail(email: String, password: String): User = translating {
        fAuth.signInWithEmailAndPassword(email, password).await().user!!.toUser()
    }

    override suspend fun signInWithGoogle(idToken: String): User = translating {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        fAuth.signInWithCredential(credential).await().user!!.toUser()
    }

    override suspend fun createUser(email: String, password: String): User = translating {
        fAuth.createUserWithEmailAndPassword(email, password).await().user!!.toUser()
    }

    override suspend fun sendEmailVerification() = translating {
        fAuth.currentUser?.sendEmailVerification()?.await()
        Unit
    }

    override suspend fun updateDisplayName(fullName: String): User = translating {
        val user = signedInUser
        user.updateProfile(userProfileChangeRequest { displayName = fullName }).await()
        user.toUser()
    }

    override suspend fun reauthenticate(email: String, password: String) = translating {
        signedInUser.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
        Unit
    }

    override suspend fun updatePassword(newPassword: String) = translating {
        signedInUser.updatePassword(newPassword).await()
        Unit
    }

    override suspend fun sendPasswordResetEmail(email: String) = translating {
        fAuth.sendPasswordResetEmail(email).await()
        Unit
    }

    override fun signOut() = fAuth.signOut()

    // Order matters: the weak-password exception is a subclass of the invalid-credentials one.
    private inline fun <T> translating(block: () -> T): T = try {
        block()
    } catch (e: AuthException) {
        throw e
    } catch (e: FirebaseAuthWeakPasswordException) {
        throw AuthException(Kind.WEAK_PASSWORD, e.errorCode, e)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        throw AuthException(Kind.INVALID_CREDENTIALS, e.errorCode, e)
    } catch (e: FirebaseAuthInvalidUserException) {
        throw AuthException(Kind.INVALID_USER, e.errorCode, e)
    } catch (e: FirebaseAuthUserCollisionException) {
        throw AuthException(Kind.USER_COLLISION, e.errorCode, e)
    } catch (e: FirebaseTooManyRequestsException) {
        throw AuthException(Kind.TOO_MANY_REQUESTS, cause = e)
    } catch (e: ApiException) {
        throw AuthException(Kind.GOOGLE_SIGN_IN, cause = e)
    } catch (e: Exception) {
        throw AuthException(Kind.OTHER, cause = e)
    }
}
