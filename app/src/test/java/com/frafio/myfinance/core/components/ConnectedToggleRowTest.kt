package com.frafio.myfinance.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.frafio.myfinance.testing.util.setThemedContent
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConnectedToggleRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val options = listOf("6M", "1Y", "2Y")
    private val selected = mutableListOf<Int>()

    private fun showRow(selectedIndex: Int) {
        composeTestRule.setThemedContent {
            Box(Modifier.requiredWidth(300.dp)) {
                ConnectedToggleRow(options = options, selectedIndex = selectedIndex, onSelect = { selected += it })
            }
        }
    }

    @Test
    fun buttons_shareTheFullWidthEqually() {
        showRow(selectedIndex = 0)

        val bounds = options.map { composeTestRule.onNodeWithText(it).getUnclippedBoundsInRoot() }

        bounds.forEach { assertThat(it.width.value).isWithin(1f).of(bounds.first().width.value) }
        assertThat(bounds.first().left.value).isWithin(1f).of(0f)
        assertThat(bounds.last().right.value).isWithin(1f).of(300f)
    }

    @Test
    fun showsEveryOption_withOnlyTheSelectedOneOn() {
        showRow(selectedIndex = 1)

        composeTestRule.onNodeWithText("6M").assertIsDisplayed().assertIsOff()
        composeTestRule.onNodeWithText("1Y").assertIsDisplayed().assertIsOn()
        composeTestRule.onNodeWithText("2Y").assertIsDisplayed().assertIsOff()
    }

    @Test
    fun clickingAnotherOption_reportsItsIndex() {
        showRow(selectedIndex = 0)

        composeTestRule.onNodeWithText("2Y").performClick()
        composeTestRule.onNodeWithText("1Y").performClick()

        assertThat(selected).containsExactly(2, 1).inOrder()
    }

    @Test
    fun clickingTheSelectedOption_reportsNothing() {
        showRow(selectedIndex = 0)

        composeTestRule.onNodeWithText("6M").performClick()

        assertThat(selected).isEmpty()
    }
}
