package com.frafio.myfinance.core.data.manager

import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.enums.auth.SignupException
import com.frafio.myfinance.core.data.remote.AuthException
import com.frafio.myfinance.core.data.remote.AuthException.Kind
import com.frafio.myfinance.testing.data.testPreferences
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.remote.TestAuthDataSource
import com.frafio.myfinance.testing.repository.TestExpensesLocalRepository
import com.frafio.myfinance.testing.repository.TestIncomesLocalRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.storage.TestProfileImageStorage
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Every `AuthCode` outcome of `AuthManager` over the fake identity provider. Robolectric only for `Log`. */
@RunWith(RobolectricTestRunner::class)
class AuthManagerTest {

    private lateinit var auth: TestAuthDataSource
    private lateinit var preferences: TestUserPreferencesRepository
    private lateinit var expenses: TestExpensesLocalRepository
    private lateinit var incomes: TestIncomesLocalRepository
    private lateinit var images: TestProfileImageStorage
    private lateinit var subject: AuthManager

    private val prefs get() = preferences.userPreferencesFlow.value

    @Before
    fun setup() {
        auth = TestAuthDataSource()
        preferences = TestUserPreferencesRepository()
        expenses = TestExpensesLocalRepository()
        incomes = TestIncomesLocalRepository()
        images = TestProfileImageStorage()
        subject = AuthManager(auth, preferences, expenses, incomes, images, UnconfinedTestDispatcher())
    }

    private fun signedIn() {
        auth.currentUser = testUser(email = "ada@example.com")
    }

    // region isUserLogged

    @Test
    fun isUserLogged_withUser_storesTheUser() = runTest {
        signedIn()

        assertThat(subject.isUserLogged().code).isEqualTo(AuthCode.USER_LOGGED.code)
        assertThat(prefs.user?.email).isEqualTo("ada@example.com")
        assertThat(subject.isUserLoggedIn()).isTrue()
    }

    @Test
    fun isUserLogged_withoutUser_reportsNotLogged() = runTest {
        assertThat(subject.isUserLogged().code).isEqualTo(AuthCode.USER_NOT_LOGGED.code)
        assertThat(prefs.user).isNull()
        assertThat(subject.isUserLoggedIn()).isFalse()
    }

    // endregion

    // region defaultLogin

    @Test
    fun defaultLogin_success_storesTheUser() = runTest {
        val result = subject.defaultLogin("ada@example.com", "password123")

        assertThat(result.code).isEqualTo(AuthCode.LOGIN_SUCCESS.code)
        assertThat(auth.signIns).containsExactly("ada@example.com" to "password123")
        assertThat(prefs.user?.email).isEqualTo("ada@example.com")
    }

    @Test
    fun defaultLogin_invalidEmail() = runTest {
        auth.signInFailure = AuthException(Kind.INVALID_CREDENTIALS, SignupException.EXCEPTION_INVALID_EMAIL.value)

        assertThat(subject.defaultLogin("ada", "password123").code).isEqualTo(AuthCode.INVALID_EMAIL.code)
        assertThat(prefs.user).isNull()
    }

    @Test
    fun defaultLogin_wrongPassword() = runTest {
        auth.signInFailure = AuthException(Kind.INVALID_CREDENTIALS, SignupException.EXCEPTION_WRONG_PASSWORD.value)

        assertThat(subject.defaultLogin("ada@example.com", "nope").code).isEqualTo(AuthCode.WRONG_PASSWORD.code)
    }

    @Test
    fun defaultLogin_otherCredentialError_isGenericFailure() = runTest {
        auth.signInFailure = AuthException(Kind.INVALID_CREDENTIALS, "ERROR_SOMETHING_ELSE")

        assertThat(subject.defaultLogin("ada@example.com", "nope").code).isEqualTo(AuthCode.LOGIN_FAILURE.code)
    }

    @Test
    fun defaultLogin_userNotFound() = runTest {
        auth.signInFailure = AuthException(Kind.INVALID_USER, SignupException.EXCEPTION_USER_NOT_FOUND.value)

        assertThat(subject.defaultLogin("ghost@example.com", "password123").code).isEqualTo(AuthCode.USER_NOT_FOUND.code)
    }

    @Test
    fun defaultLogin_userDisabled() = runTest {
        auth.signInFailure = AuthException(Kind.INVALID_USER, SignupException.EXCEPTION_USER_DISABLED.value)

        assertThat(subject.defaultLogin("ada@example.com", "password123").code).isEqualTo(AuthCode.USER_DISABLED.code)
    }

    @Test
    fun defaultLogin_networkError_isGenericFailure() = runTest {
        auth.signInFailure = AuthException(Kind.OTHER)

        assertThat(subject.defaultLogin("ada@example.com", "password123").code).isEqualTo(AuthCode.LOGIN_FAILURE.code)
    }

    // endregion

    // region Google

    @Test
    fun firebaseAuthWithGoogle_success_storesTheUser() = runTest {
        val result = subject.firebaseAuthWithGoogle("id-token")

        assertThat(result.code).isEqualTo(AuthCode.LOGIN_SUCCESS.code)
        assertThat(auth.googleSignIns).containsExactly("id-token")
        assertThat(prefs.user?.isGoogleLinked).isTrue()
    }

    @Test
    fun firebaseAuthWithGoogle_anyFailure_isGoogleLoginFailure() = runTest {
        auth.googleFailure = AuthException(Kind.GOOGLE_SIGN_IN)
        assertThat(subject.firebaseAuthWithGoogle("id-token").code).isEqualTo(AuthCode.GOOGLE_LOGIN_FAILURE.code)

        auth.googleFailure = AuthException(Kind.INVALID_CREDENTIALS, "ERROR_INVALID_CREDENTIAL")
        assertThat(subject.firebaseAuthWithGoogle("id-token").code).isEqualTo(AuthCode.GOOGLE_LOGIN_FAILURE.code)
        assertThat(prefs.user).isNull()
    }

    // endregion

    // region signup

    @Test
    fun signup_success_verifiesEmail_setsName_andStoresTheUser() = runTest {
        val result = subject.signup("Ada Lovelace", "ada@example.com", "password123")

        assertThat(result.code).isEqualTo(AuthCode.SIGNUP_SUCCESS.code)
        assertThat(auth.createdUsers).containsExactly("ada@example.com" to "password123")
        assertThat(auth.verificationsSent).isEqualTo(1)
        assertThat(auth.displayNames).containsExactly("Ada Lovelace")
        assertThat(prefs.user?.fullName).isEqualTo("Ada Lovelace")
    }

    @Test
    fun signup_weakPassword() = runTest {
        auth.createUserFailure = AuthException(Kind.WEAK_PASSWORD, "ERROR_WEAK_PASSWORD")

        assertThat(subject.signup("Ada", "ada@example.com", "123").code).isEqualTo(AuthCode.WEAK_PASSWORD.code)
        assertThat(prefs.user).isNull()
    }

    @Test
    fun signup_malformedEmail() = runTest {
        auth.createUserFailure = AuthException(Kind.INVALID_CREDENTIALS, SignupException.EXCEPTION_INVALID_EMAIL.value)

        assertThat(subject.signup("Ada", "ada", "password123").code).isEqualTo(AuthCode.EMAIL_NOT_WELL_FORMED.code)
    }

    @Test
    fun signup_emailAlreadyUsed() = runTest {
        auth.createUserFailure = AuthException(Kind.USER_COLLISION, "ERROR_EMAIL_ALREADY_IN_USE")

        assertThat(subject.signup("Ada", "ada@example.com", "password123").code).isEqualTo(AuthCode.EMAIL_ALREADY_ASSOCIATED.code)
    }

    @Test
    fun signup_otherFailure_isSignupFailure() = runTest {
        auth.createUserFailure = AuthException(Kind.OTHER)

        assertThat(subject.signup("Ada", "ada@example.com", "password123").code).isEqualTo(AuthCode.SIGNUP_FAILURE.code)
    }

    @Test
    fun signup_failureAfterCreation_isSignupFailure_andStoresNothing() = runTest {
        auth.displayNameFailure = AuthException(Kind.OTHER)

        assertThat(subject.signup("Ada", "ada@example.com", "password123").code).isEqualTo(AuthCode.SIGNUP_FAILURE.code)
        assertThat(auth.createdUsers).hasSize(1)
        assertThat(prefs.user).isNull()
    }

    // endregion

    // region resetPassword

    @Test
    fun resetPassword_success() = runTest {
        assertThat(subject.resetPassword("ada@example.com").code).isEqualTo(AuthCode.EMAIL_SENT.code)
        assertThat(auth.resetEmails).containsExactly("ada@example.com")
    }

    @Test
    fun resetPassword_tooManyRequests() = runTest {
        auth.resetFailure = AuthException(Kind.TOO_MANY_REQUESTS)

        assertThat(subject.resetPassword("ada@example.com").code).isEqualTo(AuthCode.EMAIL_NOT_SENT_TOO_MANY_REQUESTS.code)
    }

    @Test
    fun resetPassword_otherFailure() = runTest {
        auth.resetFailure = AuthException(Kind.INVALID_USER, SignupException.EXCEPTION_USER_NOT_FOUND.value)

        assertThat(subject.resetPassword("ghost@example.com").code).isEqualTo(AuthCode.EMAIL_NOT_SENT.code)
    }

    // endregion

    // region changePassword

    @Test
    fun changePassword_withCurrentPassword_reauthenticatesFirst() = runTest {
        signedIn()

        val result = subject.changePassword("newpassword", "oldpassword")

        assertThat(result.code).isEqualTo(AuthCode.PASSWORD_UPDATED.code)
        assertThat(auth.reauthentications).containsExactly("ada@example.com" to "oldpassword")
        assertThat(auth.passwordUpdates).containsExactly("newpassword")
        assertThat(prefs.user?.email).isEqualTo("ada@example.com")
    }

    @Test
    fun changePassword_withoutCurrentPassword_skipsReauthentication() = runTest {
        signedIn()

        val result = subject.changePassword("newpassword")

        assertThat(result.code).isEqualTo(AuthCode.PASSWORD_UPDATED.code)
        assertThat(auth.reauthentications).isEmpty()
        assertThat(auth.passwordUpdates).containsExactly("newpassword")
    }

    @Test
    fun changePassword_notLoggedIn() = runTest {
        assertThat(subject.changePassword("newpassword", "oldpassword").code).isEqualTo(AuthCode.USER_NOT_LOGGED.code)
        assertThat(auth.passwordUpdates).isEmpty()
    }

    @Test
    fun changePassword_wrongCurrentPassword() = runTest {
        signedIn()
        auth.reauthFailure = AuthException(Kind.INVALID_CREDENTIALS, SignupException.EXCEPTION_WRONG_PASSWORD.value)

        assertThat(subject.changePassword("newpassword", "wrong").code).isEqualTo(AuthCode.WRONG_OLD_PASSWORD.code)
        assertThat(auth.passwordUpdates).isEmpty()
    }

    @Test
    fun changePassword_weakNewPassword() = runTest {
        signedIn()
        auth.updatePasswordFailure = AuthException(Kind.WEAK_PASSWORD, "ERROR_WEAK_PASSWORD")

        assertThat(subject.changePassword("123", "oldpassword").code).isEqualTo(AuthCode.WEAK_PASSWORD.code)
    }

    @Test
    fun changePassword_otherFailure() = runTest {
        signedIn()
        auth.updatePasswordFailure = AuthException(Kind.OTHER)

        assertThat(subject.changePassword("newpassword", "oldpassword").code).isEqualTo(AuthCode.PASSWORD_NOT_UPDATED.code)
    }

    // endregion

    // region updateFullName

    @Test
    fun updateFullName_success_storesTheRenamedUser() = runTest {
        signedIn()

        val result = subject.updateFullName("Ada King")

        assertThat(result.code).isEqualTo(AuthCode.USER_FULL_NAME_UPDATED.code)
        assertThat(auth.displayNames).containsExactly("Ada King")
        assertThat(prefs.user?.fullName).isEqualTo("Ada King")
    }

    @Test
    fun updateFullName_failure() = runTest {
        signedIn()
        auth.displayNameFailure = AuthException(Kind.OTHER)

        assertThat(subject.updateFullName("Ada King").code).isEqualTo(AuthCode.USER_FULL_NAME_NOT_UPDATED.code)
        assertThat(prefs.user).isNull()
    }

    @Test
    fun updateFullName_notLoggedIn_fails() = runTest {
        assertThat(subject.updateFullName("Ada King").code).isEqualTo(AuthCode.USER_FULL_NAME_NOT_UPDATED.code)
    }

    // endregion

    @Test
    fun logout_signsOut_andClearsEverythingLocal() = runTest {
        signedIn()
        preferences.setPreferences(
            testPreferences(user = testUser(), monthlyBudget = 500.0, labels = listOf("Dinner"))
                .copy(lastExpensesSync = 10L, lastIncomesSync = 20L, lastExpensesAppSync = 30L, lastIncomesAppSync = 40L),
        )

        val result = subject.logout()

        assertThat(result.code).isEqualTo(AuthCode.LOGOUT_SUCCESS.code)
        assertThat(auth.signOutCount).isEqualTo(1)
        assertThat(expenses.deleteAllCalled).isTrue()
        assertThat(incomes.deleteAllCalled).isTrue()
        assertThat(images.deleteCount).isEqualTo(1)
        assertThat(prefs.monthlyBudget).isEqualTo(0.0)
        assertThat(prefs.labels).isEmpty()
        assertThat(prefs.lastExpensesSync).isEqualTo(0L)
        assertThat(prefs.lastIncomesSync).isEqualTo(0L)
        assertThat(prefs.lastExpensesAppSync).isEqualTo(0L)
        assertThat(prefs.lastIncomesAppSync).isEqualTo(0L)
        assertThat(preferences.clearUserDataCount).isEqualTo(1)
        assertThat(subject.isUserLoggedIn()).isFalse()
    }
}
