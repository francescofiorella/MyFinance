package com.frafio.myfinance.features.auth.components

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResetPasswordSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    private fun setSheet(show: Boolean = true, initialEmail: String = "ada@example.com") {
        composeTestRule.setThemedContent(inline = true) {
            ResetPasswordSheet(show = show, initialEmail = initialEmail, onDismiss = { events += "dismiss" }, onSend = { events += "send:$it" })
        }
    }

    private fun emailField() = composeTestRule.onNodeWithText(string(R.string.login_signup_email))
    private fun sendButton() = composeTestRule.onNodeWithContentDescription(string(R.string.send))

    @Test
    fun prefilledValidEmail_enablesSend() {
        setSheet()

        emailField().assert(hasText("ada@example.com"))
        sendButton().assertIsEnabled()
    }

    @Test
    fun invalidEmail_disablesSend() {
        setSheet(initialEmail = "")

        emailField().performTextInput("not-an-email")

        sendButton().assertIsNotEnabled()
    }

    @Test
    fun send_reportsTheTrimmedEmail_thenDismisses() {
        setSheet(initialEmail = "")
        emailField().performTextClearance()
        emailField().performTextInput("  ada@example.com  ")

        sendButton().performClick()

        assertThat(events).containsExactly("send:ada@example.com", "dismiss").inOrder()
    }

    @Test
    fun showFalse_rendersNothing() {
        setSheet(show = false)

        emailField().assertDoesNotExist()
    }
}
