package com.frafio.myfinance.core.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.frafio.myfinance.testing.util.setThemedContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SwipeableSnackbarHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun showSnackbar(message: String) {
        composeTestRule.setThemedContent {
            val hostState = remember { SnackbarHostState() }
            LaunchedEffect(Unit) { hostState.showSnackbar(message, duration = SnackbarDuration.Indefinite) }
            SwipeableSnackbarHost(hostState)
        }
    }

    @Test
    fun shownSnackbar_isDisplayed() {
        showSnackbar("Saved")

        composeTestRule.onNodeWithText("Saved").assertIsDisplayed()
    }

    @Test
    fun swipe_dismissesTheSnackbar() {
        showSnackbar("Saved")

        // Drag from the snackbar to the right edge of the screen; the text node alone is too narrow for a swipe.
        val start = composeTestRule.onNodeWithText("Saved").fetchSemanticsNode().boundsInRoot.center
        composeTestRule.onRoot().performTouchInput { swipe(start = start, end = Offset(width - 1f, start.y)) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Saved").assertDoesNotExist()
    }
}
