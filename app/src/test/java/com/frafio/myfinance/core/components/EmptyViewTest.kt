package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.frafio.myfinance.R
import com.frafio.myfinance.testing.util.setThemedContent
import com.frafio.myfinance.testing.util.string
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The illustration has no semantics, so its presence is read from where the message lands:
 * below the square image in portrait (top >= 60% of the box width), to its right in landscape
 * (left >= 30% of the box width), and centred when there is no image.
 */
@RunWith(RobolectricTestRunner::class)
class EmptyViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val box = 300.dp
    private val message = R.string.warning_home

    private fun messageBounds() = composeTestRule.onNodeWithText(string(message)).getBoundsInRoot()

    @Test
    fun noImage_showsOnlyTheMessage() {
        composeTestRule.setThemedContent {
            EmptyView(modifier = Modifier.size(box), message = message)
        }

        composeTestRule.onNodeWithText(string(message)).assertIsDisplayed()
        assertThat(messageBounds().top < box * 0.6f).isTrue()
    }

    @Test
    fun image_portrait_showsTheMessageBelowTheImage() {
        composeTestRule.setThemedContent {
            EmptyView(modifier = Modifier.size(box), image = R.drawable.image_consulting_cuate, message = message)
        }

        composeTestRule.onNodeWithText(string(message)).assertIsDisplayed()
        assertThat(messageBounds().top >= box * 0.6f).isTrue()
    }

    @Test
    @Config(qualifiers = "land")
    fun image_landscape_showsTheMessageBesideTheImage() {
        composeTestRule.setThemedContent {
            EmptyView(modifier = Modifier.size(box), image = R.drawable.image_consulting_cuate, message = message)
        }

        composeTestRule.onNodeWithText(string(message)).assertIsDisplayed()
        assertThat(messageBounds().left >= box * 0.3f).isTrue()
    }

    @Test
    fun lightTheme_withOnlyTheDarkImage_showsOnlyTheMessage() {
        composeTestRule.setThemedContent {
            EmptyView(modifier = Modifier.size(box), imageDark = R.drawable.image_investment_data_cuate, message = message)
        }

        composeTestRule.onNodeWithText(string(message)).assertIsDisplayed()
        assertThat(messageBounds().top < box * 0.6f).isTrue()
    }

    @Test
    @Config(qualifiers = "night")
    fun darkTheme_withOnlyTheDarkImage_showsIt() {
        composeTestRule.setThemedContent {
            EmptyView(modifier = Modifier.size(box), imageDark = R.drawable.image_investment_data_cuate, message = message)
        }

        assertThat(messageBounds().top >= box * 0.6f).isTrue()
    }

    @Test
    @Config(qualifiers = "night")
    fun darkTheme_withOnlyTheLightImage_fallsBackToIt() {
        composeTestRule.setThemedContent {
            EmptyView(modifier = Modifier.size(box), image = R.drawable.image_consulting_cuate, message = message)
        }

        assertThat(messageBounds().top >= box * 0.6f).isTrue()
    }
}
