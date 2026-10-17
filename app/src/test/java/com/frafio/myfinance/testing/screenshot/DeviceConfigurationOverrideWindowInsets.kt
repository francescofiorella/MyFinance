package com.frafio.myfinance.testing.screenshot

import android.view.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children

/**
 * Overrides the window insets for the content under test. Compose exposes no public override for
 * insets, so this is Now in Android's helper (Apache 2.0,
 * `app/src/testDemo/.../ui/DeviceConfigurationOverrideWindowInsets.kt`) copied verbatim: an
 * `AndroidView` whose `dispatchApplyWindowInsets` hands the children the insets we want and then
 * reports them consumed.
 *
 * Because it inserts an `AndroidView`, a screenshot must capture a tagged node rather than `onRoot()`.
 */
@Suppress("ktlint:standard:function-naming")
fun DeviceConfigurationOverride.Companion.WindowInsets(
    windowInsets: WindowInsetsCompat,
): DeviceConfigurationOverride = DeviceConfigurationOverride { contentUnderTest ->
    val currentContentUnderTest by rememberUpdatedState(contentUnderTest)
    val currentWindowInsets by rememberUpdatedState(windowInsets)
    AndroidView(
        factory = { context ->
            object : AbstractComposeView(context) {
                @Composable
                override fun Content() {
                    currentContentUnderTest()
                }

                override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
                    children.forEach {
                        it.dispatchApplyWindowInsets(WindowInsets(currentWindowInsets.toWindowInsets()))
                    }
                    return WindowInsetsCompat.CONSUMED.toWindowInsets()!!
                }

                /**
                 * Never called from our code: `View.requestApplyInsets()`, which `update` calls, delegates
                 * here. Dispatching right away lets new insets (e.g. FakeKeyboard opening) reach the content
                 * before the test asserts; without it they wait for a layout pass Robolectric hasn't run.
                 */
                @Deprecated("Deprecated in Java")
                override fun requestFitSystemWindows() {
                    dispatchApplyWindowInsets(WindowInsets(currentWindowInsets.toWindowInsets()))
                }
            }
        },
        update = { it.requestApplyInsets() },
    )
}
