package com.frafio.myfinance.features.password

import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.testing.screenshot.DefaultTestDevices
import com.frafio.myfinance.testing.screenshot.captureForDevice
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
class ChangePasswordScreenScreenshotTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun withPassword() {
        composeTestRule.captureMultiDevice("ChangePasswordScreen") { ChangePassword(hasPassword = true) }
    }

    @Test
    fun withPassword_dark() {
        composeTestRule.capturePhoneDark("ChangePasswordScreen") { ChangePassword(hasPassword = true) }
    }

    @Test
    fun withoutPassword() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ChangePasswordScreenNoPassword", deviceName = "phone") {
            ChangePassword(hasPassword = false)
        }
    }

    @Test
    fun withErrors() {
        composeTestRule.captureForDevice(DefaultTestDevices.PHONE.spec, "ChangePasswordScreenErrors", deviceName = "phone") {
            ChangePassword(hasPassword = true, currentPasswordError = "Wrong password", newPasswordError = "At least 8 characters", confirmPasswordError = "Passwords do not match")
        }
    }

    @Composable
    private fun ChangePassword(
        hasPassword: Boolean,
        currentPasswordError: String? = null,
        newPasswordError: String? = null,
        confirmPasswordError: String? = null,
    ) {
        ChangePasswordScreen(
            appState = rememberMyFinanceAppState(),
            hasPassword = hasPassword,
            isLoading = false,
            currentPasswordState = rememberTextFieldState("oldpassword"),
            currentPasswordVisible = false,
            onCurrentPasswordVisibleChange = {},
            newPasswordState = rememberTextFieldState("newpassword"),
            newPasswordVisible = false,
            onNewPasswordVisibleChange = {},
            confirmPasswordState = rememberTextFieldState("newpassword"),
            confirmPasswordVisible = false,
            onConfirmPasswordVisibleChange = {},
            onSaveClick = {},
            onBackClick = {},
            onResetPasswordClick = {},
            currentPasswordError = currentPasswordError,
            newPasswordError = newPasswordError,
            confirmPasswordError = confirmPasswordError,
        )
    }
}
