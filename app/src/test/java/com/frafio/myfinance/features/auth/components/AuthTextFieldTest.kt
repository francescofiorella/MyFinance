package com.frafio.myfinance.features.auth.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.util.setThemedContent
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val values = mutableListOf<String>()
    private val visibility = mutableListOf<Boolean>()

    @Composable
    private fun Field(
        value: String = "ada@example.com",
        error: String? = null,
        enabled: Boolean = true,
        isPassword: Boolean = false,
        passwordVisible: Boolean = false,
    ) {
        AuthTextField(
            value = value,
            onValueChange = { values += it },
            label = "Email",
            error = error,
            enabled = enabled,
            icon = R.drawable.ic_person_filled,
            isPassword = isPassword,
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
        composeTestRule.setThemedContent { Field() }

        composeTestRule.onNodeWithContentDescription("Clear").performClick()

        assertThat(values).containsExactly("")
    }

    @Test
    fun clearButton_isAbsentWhenEmptyOrDisabled() {
        composeTestRule.setThemedContent { Field(value = "") }
        composeTestRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
    }

    @Test
    fun disabledField_hasNoClearButton_andIsNotEnabled() {
        composeTestRule.setThemedContent { Field(enabled = false) }

        composeTestRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
        composeTestRule.onNodeWithTag("field").assertIsNotEnabled()
    }

    @Test
    fun error_showsTheMessage_andHidesClearAndToggle() {
        composeTestRule.setThemedContent { Field(error = "Invalid email", isPassword = true) }

        composeTestRule.onNodeWithText("Invalid email").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Show password").assertDoesNotExist()
    }

    @Test
    fun passwordField_toggleRequestsVisibility() {
        composeTestRule.setThemedContent { Field(value = "secret", isPassword = true) }

        composeTestRule.onNodeWithContentDescription("Clear").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Show password").performClick()

        assertThat(visibility).containsExactly(true)
    }

    @Test
    fun visiblePassword_offersToHideIt() {
        composeTestRule.setThemedContent { Field(value = "secret", isPassword = true, passwordVisible = true) }

        composeTestRule.onNodeWithContentDescription("Hide password").performClick()

        assertThat(visibility).containsExactly(false)
        composeTestRule.onNodeWithTag("field").assert(hasText("secret"))
    }
}
