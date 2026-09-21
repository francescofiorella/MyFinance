package com.frafio.myfinance.features.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The buttons around `AuthForm`: mode toggle, submit, forgot password, Google, back. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class AuthContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    @Composable
    private fun Content(uiState: AuthUiState = AuthUiState(), isLoading: Boolean = false) {
        AuthContent(
            appState = rememberMyFinanceAppState(),
            uiState = uiState,
            isLoading = isLoading,
            onEmailChange = {}, onPasswordChange = {}, onFullNameChange = {}, onConfirmPasswordChange = {},
            onToggleAuthMode = { events += "toggle" },
            onAuthClick = { events += "auth" },
            onGoogleClick = { events += "google" },
            onForgotPasswordClick = { events += "forgot" },
        )
    }

    @Test
    fun loginMode_buttons() {
        composeTestRule.setThemedContent { Content() }

        composeTestRule.onNodeWithTag("login_button").assertTextEquals(string(R.string.login)).performClick()
        composeTestRule.onNodeWithTag("auth_toggle_mode").assertTextEquals(string(R.string.login_signup)).performClick()
        composeTestRule.onNodeWithTag("auth_forgot_password").assertIsEnabled().performClick()
        composeTestRule.onNodeWithText(string(R.string.login_google)).performClick()
        composeTestRule.onNodeWithContentDescription(string(R.string.back_arrow)).assertDoesNotExist()

        assertThat(events).containsExactly("auth", "toggle", "forgot", "google").inOrder()
    }

    @Test
    fun signUpMode_buttons() {
        composeTestRule.setThemedContent { Content(AuthUiState(isSigningUp = true)) }

        composeTestRule.onNodeWithTag("login_button").assertTextEquals(string(R.string.signup))
        composeTestRule.onNodeWithTag("auth_toggle_mode").assertTextEquals(string(R.string.signup_login))
        composeTestRule.onNodeWithTag("auth_forgot_password").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(string(R.string.back_arrow)).performClick()

        assertThat(events).containsExactly("toggle")
    }

    @Test
    fun loading_disablesEveryButton() {
        composeTestRule.setThemedContent { Content(isLoading = true) }

        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("auth_toggle_mode").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("auth_forgot_password").assertIsNotEnabled()
        composeTestRule.onNodeWithText(string(R.string.login_google)).assertIsNotEnabled()
    }
}
