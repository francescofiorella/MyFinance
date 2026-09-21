package com.frafio.myfinance.testing.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.theme.MyFinanceTheme

/** Resolves a string resource the way the component under test does. */
fun string(@StringRes id: Int): String =
    ApplicationProvider.getApplicationContext<Context>().getString(id)

/**
 * Renders [content] under the app theme. With [inline] the sheets built on `AdaptiveSheet` render
 * their content directly instead of inside a `ModalBottomSheet`, the same path previews use.
 */
fun ComposeContentTestRule.setThemedContent(
    inline: Boolean = false,
    content: @Composable () -> Unit,
) = setContent {
    CompositionLocalProvider(LocalInspectionMode provides inline) {
        MyFinanceTheme(darkTheme = false, dynamicColor = false, content = content)
    }
}
