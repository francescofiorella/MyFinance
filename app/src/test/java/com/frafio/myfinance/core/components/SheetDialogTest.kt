package com.frafio.myfinance.core.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.MenuItem
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SheetDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // region SheetDialog

    @Test
    fun sheetDialog_showsTitleLabelAndContent() {
        composeTestRule.setThemedContent {
            SheetDialog(icon = R.drawable.ic_person_filled, title = "Your profile", label = "Edit") {
                Text("The content")
            }
        }

        composeTestRule.onNodeWithText("Your profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Edit").assertIsDisplayed()
        composeTestRule.onNodeWithText("The content").assertIsDisplayed()
    }

    @Test
    fun sheetDialog_labelFirst_putsTheLabelAboveTheTitle() {
        composeTestRule.setThemedContent {
            SheetDialog(icon = R.drawable.ic_person_filled, title = "Title", label = "Label", labelFirst = true) {}
        }

        val label = composeTestRule.onNodeWithText("Label").getBoundsInRoot()
        val title = composeTestRule.onNodeWithText("Title").getBoundsInRoot()
        assertThat(label.top < title.top).isTrue()
    }

    @Test
    fun sheetDialog_labelFirstFalse_putsTheTitleAboveTheLabel() {
        composeTestRule.setThemedContent {
            SheetDialog(icon = R.drawable.ic_person_filled, title = "Title", label = "Label", labelFirst = false) {}
        }

        val label = composeTestRule.onNodeWithText("Label").getBoundsInRoot()
        val title = composeTestRule.onNodeWithText("Title").getBoundsInRoot()
        assertThat(title.top < label.top).isTrue()
    }

    @Test
    fun sheetDialog_withoutIcon_showsTheFirstLetterOfTheTitle() {
        composeTestRule.setThemedContent {
            SheetDialog(title = "salary", label = "Label") {}
        }

        composeTestRule.onNodeWithText("S").assertIsDisplayed()
    }

    @Test
    fun sheetDialog_withIcon_showsNoLetter() {
        composeTestRule.setThemedContent {
            SheetDialog(icon = R.drawable.ic_person_filled, title = "salary", label = "Label") {}
        }

        composeTestRule.onNodeWithText("S").assertDoesNotExist()
    }

    @Test
    fun sheetDialog_endContent_isShownWhenGiven() {
        composeTestRule.setThemedContent {
            SheetDialog(title = "Title", label = "Label", endContent = "€ 0.00") {}
        }

        composeTestRule.onNodeWithText("€ 0.00").assertIsDisplayed()
    }

    // endregion

    // region ListSheetItem

    @Test
    fun listSheetItem_showsItsText() {
        composeTestRule.setThemedContent {
            ListSheetItem(item = MenuItem(iconRes = R.drawable.ic_edit_outline, textRes = R.string.edit_full_name) {}, onDismiss = {})
        }

        composeTestRule.onNodeWithText(string(R.string.edit_full_name)).assertIsDisplayed()
    }

    @Test
    fun listSheetItem_click_invokesOnClickThenOnDismiss() {
        val events = mutableListOf<String>()
        composeTestRule.setThemedContent {
            ListSheetItem(
                item = MenuItem(iconRes = R.drawable.ic_edit_outline, textRes = R.string.edit) { events += "click" },
                onDismiss = { events += "dismiss" },
            )
        }

        composeTestRule.onNodeWithText(string(R.string.edit)).performClick()

        assertThat(events).containsExactly("click", "dismiss").inOrder()
    }

    @Test
    fun listSheetItem_disabled_isNotEnabledAndIgnoresClicks() {
        val events = mutableListOf<String>()
        composeTestRule.setThemedContent {
            ListSheetItem(
                item = MenuItem(iconRes = R.drawable.ic_edit_outline, textRes = R.string.edit, enabled = false, testTag = "item") { events += "click" },
                onDismiss = { events += "dismiss" },
            )
        }

        composeTestRule.onNodeWithTag("item").assertIsNotEnabled().performClick()

        assertThat(events).isEmpty()
    }

    @Test
    fun listSheetItem_withTestTag_isFindableByTag() {
        composeTestRule.setThemedContent {
            ListSheetItem(item = MenuItem(iconRes = R.drawable.ic_edit_outline, textRes = R.string.edit, testTag = "item") {}, onDismiss = {})
        }

        composeTestRule.onNodeWithTag("item").assertIsDisplayed().assertIsEnabled()
    }

    // endregion

    // region ListSheetDialog

    @Test
    fun listSheetDialog_rendersEveryItem() {
        composeTestRule.setThemedContent {
            ListSheetDialog(
                icon = R.drawable.ic_person_filled,
                title = "Title",
                label = "Label",
                items = listOf(
                    MenuItem(iconRes = R.drawable.ic_upload_filled, textRes = R.string.edit_propic) {},
                    MenuItem(iconRes = R.drawable.ic_edit_outline, textRes = R.string.edit_full_name) {},
                ),
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText(string(R.string.edit_propic)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.edit_full_name)).assertIsDisplayed()
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(2)
    }

    // endregion

    // region GridSheetDialog

    @Test
    fun gridSheetDialog_rendersEveryItemAndTheBottomContent() {
        composeTestRule.setThemedContent {
            GridSheetDialog(
                title = "Category",
                label = "Select",
                rowSize = 3,
                items = listOf(
                    MenuItem(iconRes = R.drawable.ic_home_filled, textRes = R.string.housing) {},
                    MenuItem(iconRes = R.drawable.ic_shopping_cart_filled, textRes = R.string.groceries) {},
                ),
                onDismiss = {},
                bottomContent = { Text("Bottom") },
            )
        }

        composeTestRule.onNodeWithText(string(R.string.housing)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.groceries)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Bottom").assertIsDisplayed()
    }

    @Test
    fun gridItem_click_invokesOnClickThenOnDismiss() {
        val events = mutableListOf<String>()
        composeTestRule.setThemedContent {
            GridSheetDialog(
                title = "Category",
                label = "Select",
                rowSize = 3,
                items = listOf(MenuItem(iconRes = R.drawable.ic_home_filled, textRes = R.string.housing) { events += "click" }),
                onDismiss = { events += "dismiss" },
            )
        }

        composeTestRule.onNodeWithText(string(R.string.housing)).performClick()

        assertThat(events).containsExactly("click", "dismiss").inOrder()
    }

    @Test
    fun gridItem_symbol_isShownWithItsPlainText() {
        composeTestRule.setThemedContent {
            GridSheetDialog(
                title = "Currency",
                label = "Select",
                rowSize = 3,
                items = listOf(MenuItem(symbol = "€", text = "Euro") {}),
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText("€").assertIsDisplayed()
        composeTestRule.onNodeWithText("Euro").assertIsDisplayed()
    }

    @Test
    fun gridItem_disabled_isNotEnabled() {
        composeTestRule.setThemedContent {
            GridSheetDialog(
                title = "Category",
                label = "Select",
                rowSize = 3,
                items = listOf(MenuItem(iconRes = R.drawable.ic_home_filled, textRes = R.string.housing, enabled = false, testTag = "housing") {}),
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithTag("housing").assertIsNotEnabled()
    }

    // endregion
}
