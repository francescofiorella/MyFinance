package com.frafio.myfinance.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MenuItem(
    @DrawableRes val iconRes: Int,
    @StringRes val textRes: Int,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)