package com.frafio.myfinance.core.navigation

import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.data.model.Transaction
import kotlinx.serialization.Serializable

@Serializable
sealed interface RootKey : NavKey {
    @Serializable
    data object Auth : RootKey

    @Serializable
    data object Home : RootKey

    @Serializable
    data object Labels : RootKey

    @Serializable
    data object ChangePassword : RootKey

    @Serializable
    data object CategoriesDescription : RootKey

    @Serializable
    sealed interface RequestType {
        @Serializable
        data object Add : RequestType

        @Serializable
        data object Edit : RequestType
    }

    @Serializable
    data class AddEditTransaction(
        val requestType: RequestType,
        val expenseCode: Int,
        val transaction: Transaction? = null,
        val position: Int? = null
    ) : RootKey
}
