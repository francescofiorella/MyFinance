package com.frafio.myfinance.features.categories.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.features.categories.CategoriesScreen
import com.frafio.myfinance.core.navigation.RootKey

fun EntryProviderScope<NavKey>.categoriesDescriptionEntry(
    onBackClick: () -> Unit
) {
    entry<RootKey.CategoriesDescription> {
        CategoriesScreen(
            onBackClick = onBackClick
        )
    }
}
