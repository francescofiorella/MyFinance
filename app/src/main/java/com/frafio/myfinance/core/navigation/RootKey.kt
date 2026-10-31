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
    data class LabelDetail(val label: String) : RootKey

    @Serializable
    data object ManageLabels : RootKey

    @Serializable
    data object ChangePassword : RootKey

    @Serializable
    data object Categories : RootKey

    @Serializable
    data object ManageCategories : RootKey

    @Serializable
    data class CategoryDetail(val categoryId: Int, val year: Int, val month: Int) : RootKey

    // The expenses named [name] within [scope].
    @Serializable
    data class FrequentExpenses(val name: String, val scope: Scope) : RootKey {
        @Serializable
        sealed interface Scope {
            // A category's expenses from fromYear/fromMonth to toYear/toMonth (inclusive).
            @Serializable
            data class Category(
                val categoryId: Int,
                val fromYear: Int,
                val fromMonth: Int,
                val toYear: Int,
                val toMonth: Int
            ) : Scope

            // Every expense with the label.
            @Serializable
            data class Label(val label: String) : Scope
        }
    }

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
