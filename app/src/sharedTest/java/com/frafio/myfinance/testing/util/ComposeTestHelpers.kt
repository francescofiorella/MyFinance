package com.frafio.myfinance.testing.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ApplicationProvider
import com.frafio.myfinance.core.theme.MyFinanceTheme

/** Resolves a string resource the way the component under test does. */
fun string(@StringRes id: Int, vararg formatArgs: Any): String =
    ApplicationProvider.getApplicationContext<Context>().getString(id, *formatArgs)

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

/**
 * Invokes the node's click action without a touch. Screens emit their sheets before their content,
 * so an inline sheet sits under the full-size content and a tap would land on the content instead.
 */
fun SemanticsNodeInteraction.performClickAction(): SemanticsNodeInteraction =
    performSemanticsAction(SemanticsActions.OnClick)
