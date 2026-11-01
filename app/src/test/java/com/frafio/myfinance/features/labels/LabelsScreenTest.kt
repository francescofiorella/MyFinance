package com.frafio.myfinance.features.labels

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.frafio.myfinance.R
import com.frafio.myfinance.core.utils.doubleToPrice
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
    val composeTestRule = createComposeRule()

    private val events = mutableListOf<String>()

    private fun setScreen(groups: List<LabelGroup>? = sampleLabelGroups()) {
        composeTestRule.setThemedContent {
            LabelsContent(
                groups = groups,
                onBackClick = { events += "back" },
                onManageClick = { events += "manage" },
                onLabelClick = { events += "label:$it" }
            )
        }
    }

    @Test
    fun rows_showTotal_andDateRange_withoutTheExpenseCount() {
        setScreen()

        composeTestRule.onNodeWithText("Japan 2025").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 Feb – 19 Aug 2025").assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(3240.5)).assertIsDisplayed()
        composeTestRule.onNodeWithText("10 May 2025").assertIsDisplayed()
        composeTestRule.onNodeWithText("18 expenses", substring = true).assertDoesNotExist()
    }

    @Test
    fun groups_areTitledByYear_newestFirst_thenTheLabelsWithoutExpenses() {
        setScreen()

        val titles = listOf("2025", "2024", string(R.string.no_expenses))
        titles.forEach { title ->
            composeTestRule.onNodeWithTag("labels_list").performScrollToNode(hasText(title))
            composeTestRule.onNodeWithText(title).assertIsDisplayed()
        }
        composeTestRule.onNodeWithTag("labels_list").performScrollToNode(hasText("Paris 2026"))
        composeTestRule.onNodeWithText("Paris 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText(doubleToPrice(0.0)).assertIsDisplayed()
    }

    @Test
    fun rowsOfAGroup_keepItsOrder() {
        setScreen()

        val rows = composeTestRule.onAllNodesWithTag("label_item", useUnmergedTree = true)
        listOf("Japan 2025", "Wedding", "Home renovation").forEachIndexed { index, label ->
            rows[index].assert(hasAnyDescendant(hasText(label)))
        }
    }

    @Test
    fun noLabels_showsTheEmptyState_andManageStaysAvailable() {
        setScreen(groups = emptyList())

        composeTestRule.onNodeWithText(string(R.string.warning_labels)).assertIsDisplayed()
        composeTestRule.onNodeWithTag("labels_list").assertDoesNotExist()
        composeTestRule.onNodeWithTag("labels_manage").performClick()

        assertThat(events).containsExactly("manage")
    }

    @Test
    fun loading_showsNeitherTheListNorTheEmptyState() {
        setScreen(groups = null)

        composeTestRule.onNodeWithTag("labels_list").assertDoesNotExist()
        composeTestRule.onNodeWithText(string(R.string.warning_labels)).assertDoesNotExist()
    }

    @Test
    fun manage_andBack_invokeTheirCallbacks() {
        setScreen()

        composeTestRule.onNodeWithContentDescription(string(R.string.manage_labels)).performClick()
        composeTestRule.onNodeWithContentDescription(string(R.string.navigate_up)).performClick()

        assertThat(events).containsExactly("manage", "back").inOrder()
    }

    @Test
    fun tappingALabelWithExpenses_opensIt() {
        setScreen()

        composeTestRule.onNodeWithText("Japan 2025").performClick()

        assertThat(events).containsExactly("label:Japan 2025")
    }

    @Test
    fun aLabelWithoutExpenses_cannotBeOpened() {
        setScreen()
        composeTestRule.onNodeWithTag("labels_list").performScrollToNode(hasText("Paris 2026"))

        composeTestRule.onNode(hasTestTag("label_item") and hasAnyDescendant(hasText("Paris 2026")), useUnmergedTree = true)
            .assertHasNoClickAction()
        composeTestRule.onNodeWithText("Paris 2026").performClick()

        assertThat(events).isEmpty()
    }
}
