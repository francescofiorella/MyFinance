package com.frafio.myfinance.testing.util

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import kotlin.properties.ReadOnlyProperty

/** Resolves a string through the launched activity; evaluated lazily, after the rule has started it. */
fun AndroidComposeTestRule<*, *>.stringResource(@StringRes resId: Int): ReadOnlyProperty<Any, String> =
    ReadOnlyProperty { _, _ -> activity.getString(resId) }
