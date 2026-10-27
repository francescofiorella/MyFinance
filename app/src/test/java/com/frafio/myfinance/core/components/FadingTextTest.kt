package com.frafio.myfinance.core.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.frafio.myfinance.testing.util.setThemedContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FadingTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun textChange_endsWithOnlyTheNewText() {
        var text by mutableStateOf("2024")
        composeTestRule.setThemedContent {
            FadingText(text = text)
        }
        composeTestRule.onNodeWithText("2024").assertIsDisplayed()

        text = "2025"

        composeTestRule.onNodeWithText("2025").assertIsDisplayed()
        composeTestRule.onNodeWithText("2024").assertDoesNotExist()
    }
}
