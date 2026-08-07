package com.frafio.myfinance.core.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MenuItem(
    @DrawableRes val iconRes: Int = 0,
    @StringRes val textRes: Int = 0,
    val enabled: Boolean = true,
    val onClick: () -> Unit
) {
    var text: String? = null
    var symbol: String? = null

    constructor(
        symbol: String,
        text: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) : this(
        iconRes = 0,
        textRes = 0,
        enabled = enabled,
        onClick = onClick
    ) {
        this.symbol = symbol
        this.text = text
    }
}
