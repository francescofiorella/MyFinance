package com.frafio.myfinance.app

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.dao.ExpenseDao
import com.frafio.myfinance.core.data.repository.UserPreferencesRepository
import com.frafio.myfinance.testing.data.testExpense
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.util.stringResource
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Drives the real [MainActivity] over the Hilt test graph: fake remote repositories (the user is
 * logged in, sync completes at once), in-memory Room and DataStore.
 */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var userRepository: TestUserRepository

    @Inject
    lateinit var expenseDao: ExpenseDao

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val appName by composeTestRule.stringResource(R.string.app_name)

    @Before
    fun setup() {
        hiltRule.inject()
        // The screens show an empty state without data; seed the in-memory database and preferences.
        runBlocking {
            expenseDao.upsert(testExpense())
            userPreferencesRepository.updateLabels(listOf("Dinner"))
        }
        // The splash screen holds the first frame until checkUser() completes.
        composeTestRule.waitUntilExactlyOneExists(hasTestTag("tab_dashboard"), timeoutMillis = 10_000)
    }

    @Test
    fun firstScreen_isDashboard() {
        composeTestRule.onNodeWithTag("tab_dashboard").assertIsSelected()
        composeTestRule.onNodeWithTag("dashboard_scroll").assertExists()
    }

    @Test
    fun expensesTab_showsTheList() {
        composeTestRule.onNodeWithTag("tab_expenses").performClick()

        composeTestRule.onNodeWithTag("tab_expenses").assertIsSelected()
        composeTestRule.onNodeWithTag("expenses_list").assertExists()
    }

    @Test
    fun budgetTab_showsTheList() {
        composeTestRule.onNodeWithTag("tab_budget").performClick()

        composeTestRule.onNodeWithTag("tab_budget").assertIsSelected()
        composeTestRule.onNodeWithTag("budget_list").assertExists()
    }

    @Test
    fun profileTab_showsTheProfile() {
        composeTestRule.onNodeWithTag("tab_profile").performClick()

        composeTestRule.onNodeWithTag("tab_profile").assertIsSelected()
        composeTestRule.onNodeWithTag("profile_scroll").assertExists()
    }

    @Test
    fun topLevelDestinations_showTheAppName() {
        listOf("tab_dashboard", "tab_expenses", "tab_budget", "tab_profile").forEach { tab ->
            composeTestRule.onNodeWithTag(tab).performClick()
            composeTestRule.onAllNodesWithText(appName).onFirst().assertExists()
        }
    }

    @Test
    fun expensesTab_showsSearchAndFilter() {
        composeTestRule.onNodeWithTag("tab_expenses").performClick()

        composeTestRule.onNodeWithTag("search_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("search_filter_button").performClick()
        composeTestRule.onNodeWithTag("filter_category").assertIsDisplayed()
    }

    @Test
    fun backFromAnyTab_returnsToDashboard() {
        composeTestRule.onNodeWithTag("tab_budget").performClick()
        composeTestRule.onNodeWithTag("tab_budget").assertIsSelected()

        Espresso.pressBack()

        composeTestRule.onNodeWithTag("tab_dashboard").assertIsSelected()
    }

    @Test(expected = NoActivityResumedException::class)
    fun dashboard_back_quitsApp() {
        composeTestRule.onNodeWithTag("tab_expenses").performClick()
        composeTestRule.onNodeWithTag("tab_dashboard").performClick()

        Espresso.pressBack()
    }

    @Test
    fun addFab_opensAddScreen_andCloseReturnsToTheTab() {
        composeTestRule.onNodeWithTag("add_fab").performClick()
        composeTestRule.onNodeWithTag("add_name_field").assertIsDisplayed()

        composeTestRule.onNodeWithTag("add_close_button").performClick()

        composeTestRule.onNodeWithTag("tab_dashboard").assertIsSelected()
        composeTestRule.onNodeWithTag("add_name_field").assertDoesNotExist()
    }

    @Test
    fun profile_manageLabels_opensLabels_andBackReturns() {
        assertProfileSubScreen(entry = "profile_manage_labels", content = "labels_list")
    }

    @Test
    fun profile_manageCategories_opensCategories_andBackReturns() {
        assertProfileSubScreen(entry = "profile_manage_categories", content = "categories_scroll")
    }

    @Test
    fun profile_changePassword_opensChangePassword_andBackReturns() {
        composeTestRule.onNodeWithTag("tab_profile").performClick()
        composeTestRule.onNodeWithTag("profile_edit_profile").performClick()
        composeTestRule.onNodeWithTag("profile_scroll").performScrollToNode(hasTestTag("profile_change_password"))
        composeTestRule.onNodeWithTag("profile_change_password").performClick()
        composeTestRule.onNodeWithTag("change_password_scroll").assertExists()

        Espresso.pressBack()

        composeTestRule.onNodeWithTag("change_password_scroll").assertDoesNotExist()
        composeTestRule.onNodeWithTag("tab_profile").assertIsSelected()
    }

    @Test
    fun profile_logout_returnsToAuth() {
        composeTestRule.onNodeWithTag("tab_profile").performClick()

        composeTestRule.onNodeWithTag("home_top_bar_action").performClick()

        composeTestRule.onNodeWithTag("login_button").assertExists()
        composeTestRule.onNodeWithTag("tab_dashboard").assertDoesNotExist()
        assertThat(userRepository.logoutCount).isEqualTo(1)
    }

    private fun assertProfileSubScreen(entry: String, content: String) {
        composeTestRule.onNodeWithTag("tab_profile").performClick()
        composeTestRule.onNodeWithTag("profile_scroll").performScrollToNode(hasTestTag(entry))
        composeTestRule.onNodeWithTag(entry).performClick()
        composeTestRule.onNodeWithTag(content).assertExists()

        Espresso.pressBack()

        composeTestRule.onNodeWithTag(content).assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_scroll").assertExists()
        composeTestRule.onNodeWithTag("tab_profile").assertIsSelected()
    }
}
