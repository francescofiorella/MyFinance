package com.frafio.myfinance.core.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/** A row of a list sheet or a cell of a grid sheet. */
sealed interface MenuItem {
    val enabled: Boolean

    /** Exposed to UiAutomator as the node resource-id, for Baseline Profile generation. */
    val testTag: String?
    val onClick: () -> Unit

    data class Resource(
        @DrawableRes val iconRes: Int,
        @StringRes val textRes: Int,
        override val enabled: Boolean = true,
        override val testTag: String? = null,
        override val onClick: () -> Unit
    ) : MenuItem

    data class Symbol(
        val symbol: String,
        val text: String,
        override val enabled: Boolean = true,
        override val testTag: String? = null,
        override val onClick: () -> Unit
    ) : MenuItem
}

internal fun Modifier.menuItemTestTag(item: MenuItem): Modifier =
    item.testTag?.let { testTag(it) } ?: this

internal fun MenuItem.selectThenDismiss(onDismiss: () -> Unit): () -> Unit = {
    onClick()
    onDismiss()
}
