package com.frafio.myfinance.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface MyFinanceNavKey : NavKey {
    @Serializable
    data object Dashboard : MyFinanceNavKey

    @Serializable
    data object Expenses : MyFinanceNavKey

    @Serializable
    data object Budget : MyFinanceNavKey

    @Serializable
    data object Profile : MyFinanceNavKey
}
