package com.frafio.myfinance.core.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
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

        composeTestRule.onNodeWithTag(SWIPEABLE_SNACKBAR_TAG).performTouchInput { swipeRight() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Saved").assertDoesNotExist()
    }
}
