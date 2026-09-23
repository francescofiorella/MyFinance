package com.frafio.myfinance.core.data.remote

import com.frafio.myfinance.core.data.enums.auth.SignupException
import com.frafio.myfinance.core.data.model.User
import com.frafio.myfinance.core.data.remote.AuthException.Kind
import com.frafio.myfinance.testing.firebase.FirebaseEmulator
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.BeforeClass
import org.junit.Test
import java.util.UUID

/**
 * The real adapter against the Auth emulator: the users it builds and the `AuthException` kind and
 * code each Firebase failure becomes — the values `AuthManager` switches on. The emulator answers
 * with the legacy error codes; see docs/testing.md for what a production project may send instead.
 */
class FirebaseAuthDataSourceTest {

    companion object {
        private const val PASSWORD = "password123"

        @BeforeClass
        @JvmStatic
        fun connectToEmulator() {
            FirebaseEmulator.connect()
            FirebaseEmulator.clearAuth()
        }
    }

    private val subject = FirebaseAuthDataSource()
    private val email = FirebaseEmulator.randomEmail()

    @After
    fun teardown() {
        subject.signOut()
    }

    private suspend fun failure(block: suspend () -> Unit): AuthException {
        try {
            block()
        } catch (e: AuthException) {
            return e
        }
        throw AssertionError("expected an AuthException")
    }

    private suspend fun registered() {
        subject.createUser(email, PASSWORD)
        subject.signOut()
    }

    // region sign-up

    @Test
    fun createUser_returnsAnEmailUser_andSignsIn() = runBlocking {
        val user = subject.createUser(email, PASSWORD)

        assertThat(user.email).isEqualTo(email)
        assertThat(user.hasPassword).isTrue()
        assertThat(user.isGoogleLinked).isFalse()
        assertThat(user.provider).isEqualTo(User.EMAIL_PROVIDER)
        assertThat(subject.currentUser?.email).isEqualTo(email)
    }

    @Test
    fun createUser_existingEmail_isUserCollision() = runBlocking {
        registered()

        val e = failure { subject.createUser(email, PASSWORD) }

        assertThat(e.kind).isEqualTo(Kind.USER_COLLISION)
    }

    @Test
    fun createUser_weakPassword_isWeakPassword() = runBlocking {
        val e = failure { subject.createUser(email, "123") }

        assertThat(e.kind).isEqualTo(Kind.WEAK_PASSWORD)
    }

    @Test
    fun createUser_malformedEmail_isInvalidEmail() = runBlocking {
        val e = failure { subject.createUser("not-an-email", PASSWORD) }

        assertThat(e.kind).isEqualTo(Kind.INVALID_CREDENTIALS)
        assertThat(e.errorCode).isEqualTo(SignupException.EXCEPTION_INVALID_EMAIL.value)
    }

    // endregion

    // region sign-in

    @Test
    fun signInWithEmail_returnsTheUser() = runBlocking {
        registered()

        val user = subject.signInWithEmail(email, PASSWORD)

        assertThat(user.email).isEqualTo(email)
        assertThat(subject.currentUser?.email).isEqualTo(email)
    }

    @Test
    fun signInWithEmail_wrongPassword_isWrongPassword() = runBlocking {
        registered()

        val e = failure { subject.signInWithEmail(email, "wrong-password") }

        assertThat(e.kind).isEqualTo(Kind.INVALID_CREDENTIALS)
        assertThat(e.errorCode).isEqualTo(SignupException.EXCEPTION_WRONG_PASSWORD.value)
    }

    @Test
    fun signInWithEmail_unknownUser_isUserNotFound() = runBlocking {
        val e = failure { subject.signInWithEmail(email, PASSWORD) }

        assertThat(e.kind).isEqualTo(Kind.INVALID_USER)
        assertThat(e.errorCode).isEqualTo(SignupException.EXCEPTION_USER_NOT_FOUND.value)
    }

    @Test
    fun signInWithGoogle_returnsAGoogleUser() = runBlocking {
        // The Auth emulator accepts an unsigned JSON id token.
        val idToken = """{"sub":"google-${UUID.randomUUID()}","email":"$email","email_verified":true}"""

        val user = subject.signInWithGoogle(idToken)

        assertThat(user.email).isEqualTo(email)
        assertThat(user.isGoogleLinked).isTrue()
        assertThat(user.provider).isEqualTo(User.GOOGLE_PROVIDER)
    }

    @Test
    fun signOut_clearsTheCurrentUser() = runBlocking {
        subject.createUser(email, PASSWORD)

        subject.signOut()

        assertThat(subject.currentUser).isNull()
    }

    // endregion

    // region account changes

    @Test
    fun updateDisplayName_returnsTheRenamedUser() = runBlocking {
        subject.createUser(email, PASSWORD)

        val user = subject.updateDisplayName("Ada Lovelace")

        assertThat(user.fullName).isEqualTo("Ada Lovelace")
        assertThat(subject.currentUser?.fullName).isEqualTo("Ada Lovelace")
    }

    @Test
    fun reauthenticate_wrongPassword_isInvalidCredentials() = runBlocking {
        subject.createUser(email, PASSWORD)

        val e = failure { subject.reauthenticate(email, "wrong-password") }

        assertThat(e.kind).isEqualTo(Kind.INVALID_CREDENTIALS)
    }

    @Test
    fun reauthenticate_thenUpdatePassword_theNewPasswordSignsIn() = runBlocking {
        subject.createUser(email, PASSWORD)

        subject.reauthenticate(email, PASSWORD)
        subject.updatePassword("new-password-456")
        subject.signOut()

        assertThat(subject.signInWithEmail(email, "new-password-456").email).isEqualTo(email)
    }

    @Test
    fun updatePassword_signedOut_isInvalidUser() = runBlocking {
        val e = failure { subject.updatePassword("new-password-456") }

        assertThat(e.kind).isEqualTo(Kind.INVALID_USER)
    }

    @Test
    fun sendPasswordResetEmail_sendsAResetMail() = runBlocking {
        registered()

        subject.sendPasswordResetEmail(email)

        assertThat(FirebaseEmulator.oobCodes()).contains(email to "PASSWORD_RESET")
    }

    @Test
    fun sendEmailVerification_sendsAVerificationMail() = runBlocking {
        subject.createUser(email, PASSWORD)

        subject.sendEmailVerification()

        assertThat(FirebaseEmulator.oobCodes()).contains(email to "VERIFY_EMAIL")
    }

    // endregion
}
