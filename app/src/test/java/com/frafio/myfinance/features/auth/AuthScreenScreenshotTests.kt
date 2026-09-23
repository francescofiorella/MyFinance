package com.frafio.myfinance.features.auth

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.testing.screenshot.captureMultiDevice
import com.frafio.myfinance.testing.screenshot.capturePhoneDark
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class AuthScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun login() {
        composeTestRule.captureMultiDevice("AuthScreenLogin") { Auth(AuthUiState()) }
    }

    @Test
    fun login_dark() {
        composeTestRule.capturePhoneDark("AuthScreenLogin") { Auth(AuthUiState()) }
    }

    @Test
    fun signUp() {
        composeTestRule.captureMultiDevice("AuthScreenSignUp") {
            Auth(AuthUiState(isSigningUp = true, fullName = "Ada Lovelace", email = "ada@example.com"))
        }
    }

    @Test
    fun login_withErrors() {
        composeTestRule.captureMultiDevice("AuthScreenLoginErrors") {
            Auth(AuthUiState(email = "ada", password = "short", emailError = "Invalid email", passwordError = "At least 8 characters"))
        }
    }

    @Composable
    private fun Auth(uiState: AuthUiState) {
        AuthContent(
            appState = rememberMyFinanceAppState(),
            uiState = uiState,
            isLoading = false,
            onEmailChange = {},
            onPasswordChange = {},
            onFullNameChange = {},
            onConfirmPasswordChange = {},
            onToggleAuthMode = {},
            onAuthClick = {},
            onGoogleClick = {},
            onForgotPasswordClick = {},
        )
    }
}
