package com.frafio.myfinance.core.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.util.setThemedContent
import com.google.common.truth.Truth.assertThat
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

    @Test
    fun imageSnackbar_showsThePictureBeforeTheMessage() {
        composeTestRule.setThemedContent {
            val hostState = remember { SnackbarHostState() }
            LaunchedEffect(Unit) {
                hostState.showSnackbar(
                    ImageSnackbarVisuals(
                        message = "Welcome",
                        image = { painterResource(R.drawable.image_profile_interface_cuate) },
                        duration = SnackbarDuration.Indefinite
                    )
                )
            }
            SwipeableSnackbarHost(hostState)
        }

        val image = composeTestRule.onNodeWithTag(SNACKBAR_IMAGE_TAG).assertIsDisplayed().fetchSemanticsNode()
        val message = composeTestRule.onNodeWithText("Welcome").assertIsDisplayed().fetchSemanticsNode()
        assertThat(image.boundsInRoot.right).isAtMost(message.boundsInRoot.left)
    }

    @Test
    fun plainSnackbar_hasNoPicture() {
        showSnackbar("Saved")

        composeTestRule.onNodeWithTag(SNACKBAR_IMAGE_TAG).assertDoesNotExist()
    }
}
