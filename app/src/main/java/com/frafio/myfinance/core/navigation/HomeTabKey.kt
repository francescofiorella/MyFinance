package com.frafio.myfinance.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeTabKey : NavKey {
    @Serializable
    data object Dashboard : HomeTabKey

    @Serializable
    data object Expenses : HomeTabKey

    @Serializable
    data object Budget : HomeTabKey

    @Serializable
    data object Profile : HomeTabKey
}
