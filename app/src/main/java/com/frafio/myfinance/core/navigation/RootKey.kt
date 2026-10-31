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

    // The expenses within [scope], grouped by day.
    @Serializable
    data class FrequentExpenses(val scope: Scope) : RootKey {
        @Serializable
        sealed interface Scope {
            // The expenses named [name] of a category, from fromYear/fromMonth to toYear/toMonth (inclusive).
            @Serializable
            data class NameInCategory(
                val name: String,
                val categoryId: Int,
                val fromYear: Int,
                val fromMonth: Int,
                val toYear: Int,
                val toMonth: Int
            ) : Scope

            // The expenses named [name] with the label.
            @Serializable
            data class NameInLabel(val name: String, val label: String) : Scope

            // Every expense of a category with the label.
            @Serializable
            data class CategoryInLabel(val categoryId: Int, val label: String) : Scope
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
