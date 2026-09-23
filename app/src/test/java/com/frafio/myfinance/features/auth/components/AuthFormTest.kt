package com.frafio.myfinance.features.auth.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
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
class AuthFormTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    @Composable
    private fun Form(
        isSigningUp: Boolean = false,
        isLoading: Boolean = false,
        emailError: String? = null,
        passwordError: String? = null,
        fullNameError: String? = null,
        confirmPasswordError: String? = null,
    ) {
        // The form is stateless; echo the values back as the screen does, or Compose resets the field.
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var fullName by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        AuthForm(
            isSigningUp = isSigningUp,
            isLoading = isLoading,
            email = email, onEmailChange = { email = it; events += "email:$it" },
            password = password, onPasswordChange = { password = it; events += "password:$it" },
            fullName = fullName, onFullNameChange = { fullName = it; events += "name:$it" },
            confirmPassword = confirmPassword, onConfirmPasswordChange = { confirmPassword = it; events += "confirm:$it" },
            emailError = emailError, passwordError = passwordError,
            fullNameError = fullNameError, confirmPasswordError = confirmPasswordError,
            onAuthClick = { events += "auth" },
        )
    }

    @Test
    fun loginMode_showsOnlyEmailAndPassword() {
        composeTestRule.setThemedContent { Form() }

        composeTestRule.onNodeWithText(string(R.string.login_signup_email)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.login_password)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.signup_name)).assertDoesNotExist()
        composeTestRule.onNodeWithText(string(R.string.signup_password_confirm)).assertDoesNotExist()
    }

    @Test
    fun signUpMode_addsNameAndConfirmation_withSignUpLabels() {
        composeTestRule.setThemedContent { Form(isSigningUp = true) }

        composeTestRule.onNodeWithText(string(R.string.signup_name)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.signup_password)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.signup_password_confirm)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.login_password)).assertDoesNotExist()
    }

    @Test
    fun typing_reportsEachField() {
        composeTestRule.setThemedContent { Form(isSigningUp = true) }

        composeTestRule.onNodeWithTag("email_field").performTextInput("a")
        composeTestRule.onNodeWithTag("password_field").performTextInput("p")
        composeTestRule.onNodeWithText(string(R.string.signup_name)).performTextInput("n")
        composeTestRule.onNodeWithText(string(R.string.signup_password_confirm)).performTextInput("c")

        assertThat(events).containsExactly("email:a", "password:p", "name:n", "confirm:c")
    }

    @Test
    fun loading_disablesEveryField() {
        composeTestRule.setThemedContent { Form(isSigningUp = true, isLoading = true) }

        composeTestRule.onNodeWithTag("email_field").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("password_field").assertIsNotEnabled()
        composeTestRule.onNodeWithText(string(R.string.signup_name)).assertIsNotEnabled()
        composeTestRule.onNodeWithText(string(R.string.signup_password_confirm)).assertIsNotEnabled()
    }

    @Test
    fun imeDoneOnPassword_submits() {
        composeTestRule.setThemedContent { Form() }

        composeTestRule.onNodeWithTag("password_field").performImeAction()

        assertThat(events).containsExactly("auth")
    }

    @Test
    fun errors_areDisplayed() {
        composeTestRule.setThemedContent {
            Form(isSigningUp = true, emailError = "Bad email", passwordError = "Weak", fullNameError = "Name?", confirmPasswordError = "Mismatch")
        }

        listOf("Bad email", "Weak", "Name?", "Mismatch").forEach { composeTestRule.onNodeWithText(it).assertIsDisplayed() }
    }
}
