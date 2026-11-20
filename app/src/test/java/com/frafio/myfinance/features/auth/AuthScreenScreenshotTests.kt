package com.frafio.myfinance.features.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import com.frafio.myfinance.features.auth.components.ResetPasswordSheet
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@LooperMode(LooperMode.Mode.PAUSED)
class AuthScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun resetPasswordSheet() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ResetPasswordSheet", deviceName = "phone") {
            ResetPasswordSheet(show = true, initialEmail = "ada@example.com", onDismiss = {}, onSend = {})
        }
    }

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
            Auth(AuthUiState(isSigningUp = true), fullName = "Ada Lovelace", email = "ada@example.com")
        }
    }

    @Test
    fun login_withErrors() {
        composeTestRule.captureMultiDevice("AuthScreenLoginErrors") {
            Auth(AuthUiState(emailError = "Invalid email", passwordError = "At least 8 characters"), email = "ada", password = "short")
        }
    }

    @Composable
    private fun Auth(uiState: AuthUiState, email: String = "", password: String = "", fullName: String = "") {
        AuthContent(
            appState = rememberMyFinanceAppState(),
            uiState = uiState,
            isLoading = false,
            emailState = rememberTextFieldState(email),
            passwordState = rememberTextFieldState(password),
            fullNameState = rememberTextFieldState(fullName),
            confirmPasswordState = rememberTextFieldState(),
            onToggleAuthMode = {},
            onAuthClick = {},
            onGoogleClick = {},
            onForgotPasswordClick = {},
        )
    }
}
