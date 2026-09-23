package com.frafio.myfinance.testing.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.frafio.myfinance.testing.screenshot.WindowInsets
import com.google.common.truth.Truth.assertWithMessage

/**
 * Stands in for the soft keyboard on the JVM: wrap the screen in [Content], focus a field, then
 * [open] reports keyboard insets of [height] the way the system does once the keyboard is up.
 * The app is edge-to-edge, so a screen only makes room for the keyboard if it handles these insets.
 */
class FakeKeyboard(private val height: Dp = 300.dp) {

    private var insets by mutableStateOf(WindowInsetsCompat.Builder().build())
    private lateinit var density: Density

    @Composable
    fun Content(content: @Composable () -> Unit) {
        density = LocalDensity.current
        Box(Modifier.fillMaxSize().testTag(ROOT_TAG)) {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowInsets(insets), content)
        }
    }

    fun open() {
        val heightPx = with(density) { height.roundToPx() }
        insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, heightPx))
            .setVisible(WindowInsetsCompat.Type.ime(), true)
            .build()
    }

    fun assertAbove(rule: ComposeTestRule, node: SemanticsNodeInteraction) {
        rule.waitForIdle()
        val keyboardTop = rule.onNodeWithTag(ROOT_TAG).getBoundsInRoot().bottom - height
        val nodeBottom = node.getBoundsInRoot().bottom
        assertWithMessage("bottom of the focused field vs top of the keyboard (dp)")
            .that(nodeBottom.value).isAtMost(keyboardTop.value)
    }

    private companion object {
        const val ROOT_TAG = "fake_keyboard_root"
    }
}
