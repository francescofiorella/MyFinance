package com.frafio.myfinance.core.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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

/** Rendered inline (`LocalInspectionMode`), which bypasses `ModalBottomSheet` and shows the content directly. */
@RunWith(RobolectricTestRunner::class)
class ConfirmationSheetDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shown_displaysHeaderAndAction() {
        composeTestRule.setThemedContent(inline = true) {
            ConfirmationSheetDialog(
                headerText = R.string.delete_confirmation,
                actionIcon = R.drawable.ic_delete_outline,
                actionText = R.string.delete_permanently,
                onActionClick = {},
                show = true,
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText(string(R.string.delete_confirmation)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.delete_permanently)).assertIsDisplayed()
    }

    @Test
    fun action_invokesOnActionClickThenOnDismiss() {
        val events = mutableListOf<String>()
        composeTestRule.setThemedContent(inline = true) {
            ConfirmationSheetDialog(
                headerText = R.string.delete_confirmation,
                actionIcon = R.drawable.ic_delete_outline,
                actionText = R.string.delete_permanently,
                onActionClick = { events += "action" },
                show = true,
                onDismiss = { events += "dismiss" },
            )
        }

        composeTestRule.onNodeWithText(string(R.string.delete_permanently)).performClick()

        assertThat(events).containsExactly("action", "dismiss").inOrder()
    }

    @Test
    fun showFalse_rendersNothing() {
        composeTestRule.setThemedContent(inline = true) {
            ConfirmationSheetDialog(
                headerText = R.string.delete_confirmation,
                actionIcon = R.drawable.ic_delete_outline,
                actionText = R.string.delete_permanently,
                onActionClick = {},
                show = false,
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText(string(R.string.delete_confirmation)).assertDoesNotExist()
    }
}
