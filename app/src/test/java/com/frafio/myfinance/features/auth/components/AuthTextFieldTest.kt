package com.frafio.myfinance.features.auth.components

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val visibility = mutableListOf<Boolean>()

    @Composable
    private fun Field(
        state: TextFieldState = TextFieldState("ada@example.com"),
        error: String? = null,
        enabled: Boolean = true,
    ) {
        AuthTextField(
            state = state,
            label = "Email",
            error = error,
            enabled = enabled,
            icon = R.drawable.ic_person_filled,
            testTag = "field",
        )
    }

    @Composable
    private fun PasswordField(
        state: TextFieldState = TextFieldState("secret"),
        error: String? = null,
        passwordVisible: Boolean = false,
    ) {
        AuthPasswordField(
            state = state,
            label = "Password",
            error = error,
            enabled = true,
            icon = R.drawable.ic_password_filled,
            passwordVisible = passwordVisible,
            onPasswordVisibleChange = { visibility += it },
            testTag = "field",
        )
    }

    @Test
    fun showsLabelAndValue_andAppliesTheTag() {
        composeTestRule.setThemedContent { Field() }

        composeTestRule.onNodeWithTag("field").assert(hasText("ada@example.com"))
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun clearButton_clearsTheValue() {
        val state = TextFieldState("ada@example.com")
        composeTestRule.setThemedContent { Field(state = state) }

        composeTestRule.onNodeWithContentDescription(string(R.string.clear)).performClick()

        assertThat(state.text.toString()).isEmpty()
    }

    @Test
    fun clearButton_isAbsentWhenEmptyOrDisabled() {
        composeTestRule.setThemedContent { Field(state = TextFieldState("")) }
        composeTestRule.onNodeWithContentDescription(string(R.string.clear)).assertDoesNotExist()
    }

    @Test
    fun disabledField_hasNoClearButton_andIsNotEnabled() {
        composeTestRule.setThemedContent { Field(enabled = false) }

        composeTestRule.onNodeWithContentDescription(string(R.string.clear)).assertDoesNotExist()
        composeTestRule.onNodeWithTag("field").assertIsNotEnabled()
    }

    @Test
    fun error_showsTheMessage_andHidesClear() {
        composeTestRule.setThemedContent { Field(error = "Invalid email") }

        composeTestRule.onNodeWithText("Invalid email").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(string(R.string.clear)).assertDoesNotExist()
    }

    @Test
    fun passwordError_showsTheMessage_andHidesClearAndToggle() {
        composeTestRule.setThemedContent { PasswordField(error = "Invalid password") }

        composeTestRule.onNodeWithText("Invalid password").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(string(R.string.clear)).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(string(R.string.show_password)).assertDoesNotExist()
    }

    @Test
    fun passwordField_toggleRequestsVisibility() {
        composeTestRule.setThemedContent { PasswordField() }

        composeTestRule.onNodeWithContentDescription(string(R.string.clear)).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(string(R.string.show_password)).performClick()

        assertThat(visibility).containsExactly(true)
    }

    @Test
    fun visiblePassword_offersToHideIt() {
        composeTestRule.setThemedContent { PasswordField(passwordVisible = true) }

        composeTestRule.onNodeWithContentDescription(string(R.string.hide_password)).performClick()

        assertThat(visibility).containsExactly(false)
        composeTestRule.onNodeWithTag("field").assert(hasText("secret"))
    }
}
