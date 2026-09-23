package com.frafio.myfinance.features.password

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.frafio.myfinance.testing.util.performClickAction
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** This screen collects its own `uiEvents`, so the snackbar and `onBackClick` are observable directly. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ChangePasswordScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val userRepository = TestUserRepository()
    private var backClicks = 0
    private lateinit var appState: MyFinanceAppState

    private fun setScreen(hasPassword: Boolean = true) {
        userRepository.setUser(testUser(hasPassword = hasPassword))
        val viewModel = ChangePasswordViewModel(userRepository, LoadingRepository())
        composeTestRule.setThemedContent(inline = true) {
            appState = rememberMyFinanceAppState()
            ChangePasswordScreen(appState = appState, viewModel = viewModel, onBackClick = { backClicks++ })
        }
    }

    private fun fields() = composeTestRule.onAllNodes(hasSetTextAction())
    private fun save() = composeTestRule.onNodeWithContentDescription(string(R.string.save)).performClick()

    private fun fill(current: String? = "oldpassword", new: String = "newpassword", confirm: String = "newpassword") {
        var index = 0
        if (current != null) fields()[index++].performTextInput(current)
        fields()[index++].performTextInput(new)
        fields()[index].performTextInput(confirm)
    }

    @Test
    fun save_withEmptyFields_showsErrors() {
        setScreen()

        save()

        composeTestRule.onNodeWithText(AuthCode.EMPTY_PASSWORD.message).assertIsDisplayed()
        composeTestRule.onNodeWithText(AuthCode.EMPTY_NEW_PASSWORD.message).assertIsDisplayed()
        composeTestRule.onNodeWithText(AuthCode.EMPTY_CONFIRM_NEW_PASSWORD.message).assertIsDisplayed()
        assertThat(userRepository.changePasswordCalls).isEmpty()
    }

    @Test
    fun save_withShortNewPassword_showsShortPassword() {
        setScreen()
        fill(new = "short", confirm = "short")

        save()

        composeTestRule.onNodeWithText(AuthCode.SHORT_PASSWORD.message).assertIsDisplayed()
    }

    @Test
    fun save_withMismatch_showsPasswordNotMatch() {
        setScreen()
        fill(confirm = "newpassword2")

        save()

        composeTestRule.onNodeWithText(AuthCode.PASSWORD_NOT_MATCH.message).assertIsDisplayed()
        assertThat(userRepository.changePasswordCalls).isEmpty()
    }

    @Test
    fun save_valid_changesThePassword_thenGoesBack() {
        setScreen()
        fill()

        save()

        assertThat(userRepository.changePasswordCalls).containsExactly("newpassword" to "oldpassword")
        assertThat(backClicks).isEqualTo(1)
        assertThat(appState.snackbarHostState.currentSnackbarData?.visuals?.message).isEqualTo(AuthCode.PASSWORD_UPDATED.message)
    }

    @Test
    fun wrongCurrentPassword_showsTheRemoteError_andStays() {
        setScreen()
        userRepository.changePasswordResult = AuthResult(AuthCode.WRONG_OLD_PASSWORD)
        fill()

        save()

        composeTestRule.onNodeWithText(AuthCode.WRONG_OLD_PASSWORD.message).assertIsDisplayed()
        assertThat(backClicks).isEqualTo(0)
    }

    @Test
    fun resetPassword_asksForConfirmation_thenSendsTheEmail() {
        setScreen()

        composeTestRule.onNodeWithText(string(R.string.reset_password)).performClick()
        assertThat(userRepository.resetPasswordCalls).isEmpty()
        composeTestRule.onNodeWithText(string(R.string.send_email)).performClickAction()

        assertThat(userRepository.resetPasswordCalls).containsExactly("ada@example.com")
    }

    @Test
    fun userWithoutPassword_hasNoCurrentField_andSendsNull() {
        setScreen(hasPassword = false)

        fields().assertCountEquals(2)
        fill(current = null)
        save()

        assertThat(userRepository.changePasswordCalls).containsExactly("newpassword" to null)
    }
}
