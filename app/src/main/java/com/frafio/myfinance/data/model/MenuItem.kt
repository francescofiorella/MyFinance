package com.frafio.myfinance.data.model

import androidx.annotation.DrawableRes

data class MenuItem(
    @DrawableRes val icon: Int,
    val text: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)