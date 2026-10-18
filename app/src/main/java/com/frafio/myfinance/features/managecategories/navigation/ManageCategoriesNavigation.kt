package com.frafio.myfinance.features.managecategories.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.managecategories.ManageCategoriesScreen

fun EntryProviderScope<NavKey>.manageCategoriesEntry(
    onBackClick: () -> Unit
) {
    entry<RootKey.ManageCategories> {
        ManageCategoriesScreen(onBackClick = onBackClick)
    }
}
