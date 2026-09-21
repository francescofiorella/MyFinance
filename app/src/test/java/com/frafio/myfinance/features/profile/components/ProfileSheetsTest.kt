package com.frafio.myfinance.features.profile.components

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.capitalizeWords
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Currency

/** `EditFullNameSheet`, `CurrencySheet` and `SelectProPicSheet`, rendered inline. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ProfileSheetsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    // region EditFullNameSheet

    private fun setNameSheet(fullName: String = "Ada Lovelace") {
        composeTestRule.setThemedContent(inline = true) {
            EditFullNameSheet(show = true, fullName = fullName, onDismiss = { events += "dismiss" }, onEditFullName = { events += "name:$it" })
        }
    }

    private fun nameField() = composeTestRule.onNodeWithText(string(R.string.signup_name))
    private fun confirm() = composeTestRule.onNodeWithContentDescription(string(R.string.confirm))

    @Test
    fun editFullName_prefilled_andUnchangedNameKeepsConfirmDisabled() {
        setNameSheet()

        nameField().assert(hasText("Ada Lovelace"))
        confirm().assertIsNotEnabled()
    }

    @Test
    fun editFullName_blankOrSameWithSpaces_keepsConfirmDisabled() {
        setNameSheet()

        nameField().performTextClearance()
        nameField().performTextInput("   ")
        confirm().assertIsNotEnabled()

        nameField().performTextClearance()
        nameField().performTextInput("  Ada Lovelace  ")
        confirm().assertIsNotEnabled()
    }

    @Test
    fun editFullName_changedName_isSentTrimmed_thenDismisses() {
        setNameSheet()
        nameField().performTextClearance()
        nameField().performTextInput("  Ada King ")

        confirm().assertIsEnabled().performClick()

        assertThat(events).containsExactly("name:Ada King", "dismiss").inOrder()
    }

    // endregion

    // region CurrencySheet

    private fun currencyName(code: String) = Currency.getInstance(code).displayName.capitalizeWords()

    @Test
    fun currencySheet_listsTheBaseCurrencies_andOffersToShowAll() {
        composeTestRule.setThemedContent(inline = true) {
            CurrencySheet(show = true, onDismiss = {}, onCurrencySelected = {})
        }

        listOf("EUR", "USD", "GBP", "JPY").forEach { composeTestRule.onNodeWithText(currencyName(it)).assertIsDisplayed() }
        composeTestRule.onNodeWithText(currencyName("AED")).assertDoesNotExist()

        composeTestRule.onNodeWithText(string(R.string.show_all_currencies)).performClick()

        composeTestRule.onNodeWithText(string(R.string.show_all_currencies)).assertDoesNotExist()
        composeTestRule.onNodeWithText(currencyName("AED")).assertExists()
    }

    @Test
    fun currencySheet_select_reportsTheCode_thenDismisses() {
        composeTestRule.setThemedContent(inline = true) {
            CurrencySheet(show = true, onDismiss = { events += "dismiss" }, onCurrencySelected = { events += "currency:$it" })
        }

        composeTestRule.onNodeWithText(currencyName("USD")).performClick()

        assertThat(events).containsExactly("currency:USD", "dismiss").inOrder()
    }

    @Test
    fun currencySheet_showFalse_rendersNothing() {
        composeTestRule.setThemedContent(inline = true) {
            CurrencySheet(show = false, onDismiss = {}, onCurrencySelected = {})
        }

        composeTestRule.onNodeWithText(string(R.string.currency)).assertDoesNotExist()
    }

    // endregion

    // region SelectProPicSheet

    private fun setProPicSheet(googlePhotoUrl: String?, current: String?) {
        composeTestRule.setThemedContent(inline = true) {
            SelectProPicSheet(show = true, googlePhotoUrl = googlePhotoUrl, currentProPic = current, onDismiss = {}, onSelectPhoto = { events += "photo:$it" })
        }
    }

    private fun pictures() = composeTestRule.onAllNodesWithContentDescription(string(R.string.profile_picture))

    @Test
    fun proPicSheet_offersTheSevenAvatars_andDisablesTheCurrentOne() {
        setProPicSheet(googlePhotoUrl = null, current = "avatar_1")

        pictures().assertCountEquals(7)
        pictures()[0].assertIsNotEnabled()
        pictures()[2].assertIsEnabled().performClick()

        assertThat(events).containsExactly("photo:avatar_3")
    }

    @Test
    fun proPicSheet_withGooglePhoto_addsItFirst() {
        setProPicSheet(googlePhotoUrl = "https://example.com/photo.jpg", current = "google")

        pictures().assertCountEquals(8)
        pictures()[0].assertIsNotEnabled()
        pictures()[1].performClick()

        assertThat(events).containsExactly("photo:avatar_1")
    }

    @Test
    fun proPicSheet_allClickable() {
        setProPicSheet(googlePhotoUrl = null, current = null)

        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(7)
    }

    // endregion
}
