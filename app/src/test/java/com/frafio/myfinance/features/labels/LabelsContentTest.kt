package com.frafio.myfinance.features.labels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.screenshot.WindowInsets
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class LabelsContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    private fun setLabels(labels: List<String>) {
        composeTestRule.setThemedContent {
            LabelsContent(
                allLabels = labels,
                onBackClick = { events += "back" },
                onAddLabel = { events += "add:$it" },
                onDeleteLabel = { events += "delete:$it" },
                onEditLabel = { old, new -> events += "edit:$old>$new" },
                snackbarHost = {},
            )
        }
    }

    /** The "create label" field is the first text field; while editing, the row's field comes after it. */
    private fun newLabelField() = composeTestRule.onAllNodes(hasSetTextAction())[0]
    private fun editField() = composeTestRule.onAllNodes(hasSetTextAction())[1]
    private fun confirm() = composeTestRule.onNodeWithContentDescription(string(R.string.confirm))

    @Test
    fun withLabels_showsTheList_andBack() {
        setLabels(listOf("Food", "Rent"))

        composeTestRule.onNodeWithTag("labels_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rent").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(events).containsExactly("back")
    }

    @Test
    fun withoutLabels_showsTheEmptyView() {
        setLabels(emptyList())

        composeTestRule.onNodeWithText(string(R.string.warning_labels)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("labels_list").assertDoesNotExist()
    }

    @Test
    fun addLabel_confirmAppearsWhenTyping_andReportsTheTrimmedName() {
        setLabels(listOf("Food"))
        composeTestRule.onNodeWithText(string(R.string.create_label)).assertIsDisplayed()
        confirm().assertDoesNotExist()

        newLabelField().performTextInput("  Gift ")
        confirm().assertIsEnabled().performClick()

        assertThat(events).containsExactly("add:Gift")
        composeTestRule.onNodeWithText(string(R.string.create_label)).assertIsDisplayed()
    }

    @Test
    fun addLabel_duplicateIgnoringCase_isRejected() {
        setLabels(listOf("Food"))

        newLabelField().performTextInput("food")

        confirm().assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(string(R.string.clear)).performClick()
        confirm().assertDoesNotExist()
    }

    @Test
    fun editLabel_unchangedName_keepsConfirmDisabled() {
        setLabels(listOf("Food", "Rent"))

        composeTestRule.onAllNodesWithContentDescription(string(R.string.edit))[0].performClick()

        confirm().assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription(string(R.string.close)).assertIsDisplayed()
    }

    @Test
    fun editLabel_newName_isReported() {
        setLabels(listOf("Food", "Rent"))
        composeTestRule.onAllNodesWithContentDescription(string(R.string.edit))[0].performClick()

        editField().performTextClearance()
        editField().performTextInput("Meals")
        confirm().assertIsEnabled().performClick()

        assertThat(events).containsExactly("edit:Food>Meals")
        composeTestRule.onNodeWithContentDescription(string(R.string.close)).assertDoesNotExist()
    }

    @Test
    fun editLabel_close_restoresTheRow() {
        setLabels(listOf("Food"))
        composeTestRule.onAllNodesWithContentDescription(string(R.string.edit))[0].performClick()

        composeTestRule.onNodeWithContentDescription(string(R.string.close)).performClick()

        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription(string(R.string.edit))[0].assertIsDisplayed()
        assertThat(events).isEmpty()
    }

    @Test
    fun deleteLabel_isReported() {
        setLabels(listOf("Food", "Rent"))

        composeTestRule.onAllNodesWithContentDescription(string(R.string.delete))[1].performClick()

        assertThat(events).containsExactly("delete:Rent")
    }

    @Test
    fun editingTheLastLabel_keepsTheFieldAboveTheKeyboard() {
        val keyboardHeight = 300.dp
        var insets by mutableStateOf(WindowInsetsCompat.Builder().build())
        lateinit var density: Density
        composeTestRule.setThemedContent {
            density = LocalDensity.current
            Box(Modifier.fillMaxSize().testTag("root")) {
                DeviceConfigurationOverride(DeviceConfigurationOverride.WindowInsets(insets)) {
                    LabelsContent(
                        allLabels = (1..20).map { "Label $it" },
                        onBackClick = {},
                        onAddLabel = {},
                        onDeleteLabel = {},
                        onEditLabel = { _, _ -> },
                        snackbarHost = {},
                    )
                }
            }
        }
        composeTestRule.onNodeWithTag("labels_list").performScrollToNode(hasText("Label 20"))
        // A lazy list's semantics order is not its visual order: take the Edit button in Label 20's row.
        val row = composeTestRule.onNodeWithText("Label 20").fetchSemanticsNode().boundsInRoot
        val editButtons = composeTestRule.onAllNodesWithContentDescription(string(R.string.edit))
        val inRow = editButtons.fetchSemanticsNodes().indexOfFirst { it.boundsInRoot.center.y in row.top..row.bottom }
        editButtons[inRow].performClick()
        editField().assert(hasText("Label 20"))

        // The keyboard opens once the field has focus, as on a phone.
        val keyboardPx = with(density) { keyboardHeight.roundToPx() }
        insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, keyboardPx))
            .setVisible(WindowInsetsCompat.Type.ime(), true)
            .build()
        composeTestRule.waitForIdle()

        val keyboardTop = composeTestRule.onNodeWithTag("root").getBoundsInRoot().bottom - keyboardHeight
        assertThat(editField().getBoundsInRoot().bottom.value).isAtMost(keyboardTop.value)
    }
}
