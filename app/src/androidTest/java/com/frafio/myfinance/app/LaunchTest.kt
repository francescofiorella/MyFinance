package com.frafio.myfinance.app

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.auth.AuthCode
import com.frafio.myfinance.core.data.model.AuthResult
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/** Launches [MainActivity] by hand so the fakes can be seeded before the activity exists. */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class LaunchTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createEmptyComposeRule()

    @Inject
    lateinit var userRepository: TestUserRepository

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun teardown() {
        scenario?.close()
    }

    private fun launch(action: String? = null) {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).setAction(action)
        scenario = ActivityScenario.launch(intent)
    }

    private fun setLoggedOut() {
        userRepository.loggedIn = false
        userRepository.isUserLoggedResult = AuthResult(AuthCode.USER_NOT_LOGGED)
    }

    @Test
    fun loggedOut_startsOnAuth() {
        setLoggedOut()

        launch()

        composeTestRule.waitUntilExactlyOneExists(hasTestTag("login_button"))
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_dashboard").assertDoesNotExist()
    }

    @Test
    fun login_success_navigatesToDashboard() {
        setLoggedOut()
        launch()
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("login_button"))

        composeTestRule.onNodeWithTag("email_field").performTextInput("ada@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        // The fake accepts the credentials; the session it would open is what checkUser() reads next.
        userRepository.loggedIn = true
        userRepository.isUserLoggedResult = AuthResult(AuthCode.USER_LOGGED)
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.waitUntilExactlyOneExists(hasTestTag("tab_dashboard"))
        composeTestRule.onNodeWithTag("tab_dashboard").assertIsSelected()
        assertThat(userRepository.loginCalls).containsExactly("ada@example.com" to "password123")
    }

    @Test
    fun login_failure_staysOnAuth() {
        setLoggedOut()
        userRepository.loginResult = AuthResult(AuthCode.WRONG_PASSWORD)
        launch()
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("login_button"))

        composeTestRule.onNodeWithTag("email_field").performTextInput("ada@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("login_button").performClick()

        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_dashboard").assertDoesNotExist()
        assertThat(userRepository.loginCalls).hasSize(1)
    }

    @Test
    fun freshStart_withoutData_showsTheEmptyDashboard() {
        launch()

        composeTestRule.waitUntilExactlyOneExists(hasTestTag("tab_dashboard"))
        val message = ApplicationProvider.getApplicationContext<Context>().getString(R.string.warning_home)
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
        composeTestRule.onNodeWithTag("dashboard_scroll").assertDoesNotExist()
    }

    @Test
    fun shortcut_addExpense_opensAddScreen() {
        launch(action = "com.frafio.myfinance.ADD_EXPENSE")

        composeTestRule.waitUntilExactlyOneExists(hasTestTag("add_name_field"))
        composeTestRule.onNodeWithTag("add_name_field").assertIsDisplayed()
    }

    @Test
    fun shortcut_addIncome_opensAddScreen() {
        launch(action = "com.frafio.myfinance.ADD_INCOME")

        composeTestRule.waitUntilExactlyOneExists(hasTestTag("add_name_field"))
        composeTestRule.onNodeWithTag("add_name_field").assertIsDisplayed()
    }
}
