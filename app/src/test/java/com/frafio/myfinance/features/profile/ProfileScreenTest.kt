package com.frafio.myfinance.features.profile

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.utils.capitalizeWords
import com.frafio.myfinance.testing.data.testUser
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
import com.frafio.myfinance.testing.repository.TestUserRepository
import com.frafio.myfinance.testing.storage.TestProfileImageStorage
import com.frafio.myfinance.testing.util.MainDispatcherRule
import com.frafio.myfinance.testing.util.performClickAction
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Currency

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ProfileScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val userRepository = TestUserRepository()
    private val expensesRepository = TestExpensesRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private lateinit var viewModel: ProfileViewModel

    private val events = mutableListOf<ProfileUiEvent>()
    private val navigation = mutableListOf<String>()

    @Before
    fun setup() {
        userRepository.setUser(testUser(fullName = "Ada Lovelace"))
        viewModel = ProfileViewModel(userRepository, expensesRepository, userPreferencesRepository, LoadingRepository(), TestProfileImageStorage())
        composeTestRule.setThemedContent(inline = true) {
            LaunchedEffect(Unit) { viewModel.uiEvents.collect { events += it } }
            ProfileScreen(
                viewModel = viewModel,
                onManageLabels = { navigation += "labels" },
                onCategoriesDescriptionClick = { navigation += "categories" },
                onChangePassword = { navigation += "password" },
            )
        }
    }

    private fun scrollTo(tag: String) = composeTestRule.onNodeWithTag("profile_scroll").performScrollToNode(hasTestTag(tag))

    @Test
    fun showsNameEmailAndVersion() {
        composeTestRule.onNodeWithText("Ada Lovelace").assertIsDisplayed()
        composeTestRule.onNodeWithText("ada@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText(viewModel.versionName).assertExists()
    }

    @Test
    fun editProfile_expands_andEditFullNameSaves() {
        composeTestRule.onNodeWithText(string(R.string.edit_full_name)).assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_edit_profile").performClick()

        composeTestRule.onNodeWithText(string(R.string.edit_full_name)).performClick()
        val field = composeTestRule.onNodeWithText(string(R.string.signup_name))
        field.performTextClearance()
        field.performTextInput("Ada King")
        composeTestRule.onNodeWithContentDescription(string(R.string.confirm)).performClickAction()

        assertThat(userRepository.fullNameUpdates).containsExactly("Ada King")
        assertThat(events).containsExactly(ProfileUiEvent.FullNameUpdated("Ada Lovelace"))
    }

    @Test
    fun changeCurrency_opensTheSheet_andSelects() {
        scrollTo("profile_change_currency")
        composeTestRule.onNodeWithTag("profile_change_currency").performClick()

        composeTestRule.onNodeWithText(Currency.getInstance("USD").displayName.capitalizeWords()).performClickAction()

        assertThat(expensesRepository.currencyCodeCalls).containsExactly("USD")
    }

    @Test
    fun dynamicColourSwitch_reportsTheChange() {
        scrollTo("profile_change_currency")

        composeTestRule.onNode(isToggleable()).performClick()

        assertThat(expensesRepository.dynamicColorCalls).containsExactly(false)
    }

    @Test
    fun subScreens_invokeTheirCallbacks() {
        composeTestRule.onNodeWithTag("profile_manage_labels").performClick()
        composeTestRule.onNodeWithTag("profile_manage_categories").performClick()
        composeTestRule.onNodeWithTag("profile_edit_profile").performClick()
        scrollTo("profile_change_password")
        composeTestRule.onNodeWithTag("profile_change_password").performClick()

        assertThat(navigation).containsExactly("labels", "categories", "password").inOrder()
    }

    @Test
    fun selectProfilePicture_reportsTheChoice() {
        composeTestRule.onNodeWithTag("profile_edit_profile").performClick()
        composeTestRule.onNodeWithText(string(R.string.edit_propic)).performClick()

        composeTestRule.onAllNodesWithContentDescription(string(R.string.profile_picture))[2].performClickAction()

        assertThat(expensesRepository.proPicChoiceCalls).containsExactly("avatar_3")
    }
}
