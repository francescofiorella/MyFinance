package com.frafio.myfinance.features.auth

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The stateful screen over a directly built ViewModel; `uiEvents` are collected here as the navigation entry does. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class AuthScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val userRepository = TestUserRepository()
    private val events = mutableListOf<AuthUiEvent>()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        viewModel = AuthViewModel(userRepository, LoadingRepository())
        composeTestRule.setThemedContent(inline = true) {
            LaunchedEffect(Unit) { viewModel.uiEvents.collect { events += it } }
            AuthScreen(appState = rememberMyFinanceAppState(), viewModel = viewModel)
        }
    }

    private fun typeCredentials(email: String, password: String) {
        composeTestRule.onNodeWithTag("email_field").performTextInput(email)
        composeTestRule.onNodeWithTag("password_field").performTextInput(password)
    }

    @Test
    fun login_withEmptyFields_showsValidationErrors() {
        composeTestRule.onNodeWithTag("login_button").performClick()

        // The message equals the field label, so pick the node that is not the text field.
        composeTestRule.onAllNodesWithText(AuthCode.EMPTY_EMAIL.message).filterToOne(!hasSetTextAction()).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(AuthCode.EMPTY_PASSWORD.message).filterToOne(!hasSetTextAction()).assertIsDisplayed()
        assertThat(userRepository.loginCalls).isEmpty()
    }

    @Test
    fun login_withShortPassword_showsShortPasswordError() {
        typeCredentials("ada@example.com", "short")

        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.onNodeWithText(AuthCode.SHORT_PASSWORD.message).assertIsDisplayed()
        assertThat(userRepository.loginCalls).isEmpty()
    }

    @Test
    fun login_valid_callsTheRepository_andEmitsSuccess() {
        typeCredentials("ada@example.com", "password123")

        composeTestRule.onNodeWithTag("login_button").performClick()

        assertThat(userRepository.loginCalls).containsExactly("ada@example.com" to "password123")
        assertThat(events).containsExactly(AuthUiEvent.Success)
    }

    @Test
    fun login_failure_emitsTheError() {
        userRepository.loginResult = AuthResult(AuthCode.WRONG_PASSWORD)
        typeCredentials("ada@example.com", "password123")

        composeTestRule.onNodeWithTag("login_button").performClick()

        assertThat(events).containsExactly(AuthUiEvent.Error(AuthCode.WRONG_PASSWORD.message))
    }

    @Test
    fun signUp_withMismatchedPasswords_showsTheError() {
        composeTestRule.onNodeWithTag("auth_toggle_mode").performClick()
        composeTestRule.onNodeWithText(string(R.string.signup_name)).performTextInput("Ada Lovelace")
        typeCredentials("ada@example.com", "password123")
        composeTestRule.onNodeWithText(string(R.string.signup_password_confirm)).performTextInput("password124")

        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.onNodeWithText(AuthCode.PASSWORD_NOT_MATCH.message).assertIsDisplayed()
        assertThat(userRepository.signupCalls).isEmpty()
    }

    @Test
    fun forgotPassword_opensTheResetSheet_prefilled_andSends() {
        composeTestRule.onNodeWithTag("email_field").performTextInput("ada@example.com")

        composeTestRule.onNodeWithTag("auth_forgot_password").performClick()
        composeTestRule.onNodeWithContentDescription(string(R.string.send)).performClick()

        assertThat(userRepository.resetPasswordCalls).containsExactly("ada@example.com")
        assertThat(events).containsExactly(AuthUiEvent.Message(AuthCode.EMAIL_SENT.message))
    }
}
