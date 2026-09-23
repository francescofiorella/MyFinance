package com.frafio.myfinance.core.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.util.setThemedContent
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Only the drawable overload: the URL one goes through Coil's `AsyncImage`. */
@RunWith(RobolectricTestRunner::class)
class ImageSelectorButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun unselected_isEnabledAndReportsClicks() {
        var clicks = 0
        composeTestRule.setThemedContent {
            ImageSelectorButton(drawable = R.drawable.image_profile_interface_cuate, onClick = { clicks++ }, contentDescription = "Avatar")
        }

        composeTestRule.onNode(hasClickAction()).assertIsEnabled().performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun selected_isNotEnabled() {
        var clicks = 0
        composeTestRule.setThemedContent {
            ImageSelectorButton(drawable = R.drawable.image_profile_interface_cuate, onClick = { clicks++ }, contentDescription = "Avatar", isSelected = true)
        }

        composeTestRule.onNode(hasClickAction()).assertIsNotEnabled().performClick()

        assertThat(clicks).isEqualTo(0)
    }

    @Test
    fun contentDescription_isApplied() {
        composeTestRule.setThemedContent {
            ImageSelectorButton(drawable = R.drawable.image_profile_interface_cuate, onClick = {}, contentDescription = "Avatar")
        }

        composeTestRule.onNodeWithContentDescription("Avatar").assertIsDisplayed()
    }
}
