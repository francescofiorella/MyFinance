package com.frafio.myfinance.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.data.model.Transaction
import kotlinx.serialization.Serializable

@Serializable
sealed interface RootKey : NavKey {
    @Serializable
    data object Home : RootKey

    @Serializable
    data class AddEditTransaction(
        val requestCode: Int,
        val expenseCode: Int,
        val transaction: Transaction? = null,
        val position: Int? = null
    ) : RootKey
}
