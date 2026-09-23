package com.frafio.myfinance.features.labels

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.enums.db.FinanceCode
import com.frafio.myfinance.core.data.repository.LoadingRepository
import com.frafio.myfinance.core.navigation.rememberMyFinanceAppState
import com.frafio.myfinance.testing.repository.TestExpensesRepository
import com.frafio.myfinance.testing.repository.TestUserPreferencesRepository
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

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class LabelsScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val expensesRepository = TestExpensesRepository()
    private val userPreferencesRepository = TestUserPreferencesRepository()
    private val events = mutableListOf<LabelsUiEvent>()
    private var backClicks = 0

    private fun setScreen(labels: List<String> = listOf("food", "travel")) {
        userPreferencesRepository.setLabels(labels)
        val viewModel = LabelsViewModel(expensesRepository, LoadingRepository(), userPreferencesRepository)
        composeTestRule.setThemedContent(inline = true) {
            LaunchedEffect(Unit) { viewModel.uiEvents.collect { events += it } }
            LabelsScreen(appState = rememberMyFinanceAppState(), viewModel = viewModel, onBackClick = { backClicks++ })
        }
    }

    private fun confirm() = composeTestRule.onNodeWithContentDescription(string(R.string.confirm))

    @Test
    fun showsTheLabels_fromPreferences_andBack() {
        setScreen()

        composeTestRule.onNodeWithTag("labels_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("food").assertIsDisplayed()
        composeTestRule.onNodeWithText("travel").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()
        assertThat(backClicks).isEqualTo(1)
    }

    @Test
    fun noLabels_showsTheEmptyView() {
        setScreen(emptyList())

        composeTestRule.onNodeWithText(string(R.string.warning_labels)).assertIsDisplayed()
    }

    @Test
    fun addLabel_callsTheRepository_andEmitsTheSnackbar() {
        setScreen()

        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Gift")
        confirm().performClick()

        assertThat(expensesRepository.addedLabels).containsExactly("Gift")
        composeTestRule.waitForIdle()
        assertThat(events).containsExactly(LabelsUiEvent.ShowSnackBar(FinanceCode.LABEL_ADD_SUCCESS.message))
    }

    @Test
    fun deleteLabel_asksForConfirmation_thenDeletes_andEmitsTheUndoEvent() {
        setScreen()

        composeTestRule.onAllNodesWithContentDescription(string(R.string.delete))[1].performClick()
        assertThat(expensesRepository.deletedLabels).isEmpty()
        composeTestRule.onNodeWithText(string(R.string.delete_permanently)).performClickAction()

        assertThat(expensesRepository.deletedLabels).containsExactly("travel")
        composeTestRule.waitForIdle()
        assertThat(events).containsExactly(LabelsUiEvent.LabelDeleted("travel", emptyList(), FinanceCode.LABEL_DELETE_SUCCESS.message))
    }

    @Test
    fun editLabel_saves() {
        setScreen()

        composeTestRule.onAllNodesWithContentDescription(string(R.string.edit))[0].performClick()
        val field = composeTestRule.onAllNodes(hasSetTextAction())[1]
        field.performTextClearance()
        field.performTextInput("meals")
        confirm().performClick()

        assertThat(expensesRepository.editedLabels).containsExactly("food" to "meals")
    }
}
