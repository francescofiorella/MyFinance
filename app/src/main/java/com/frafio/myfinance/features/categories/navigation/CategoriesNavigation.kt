package com.frafio.myfinance.features.categories.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.categories.CategoriesScreen
import com.frafio.myfinance.features.categories.CategoriesViewModel

fun EntryProviderScope<NavKey>.categoriesEntry(
    onBackClick: () -> Unit,
    onCategoryClick: (RootKey.CategoryDetail) -> Unit
) {
    entry<RootKey.Categories> {
        val viewModel: CategoriesViewModel = hiltViewModel()
        CategoriesScreen(
            viewModel = viewModel,
            onBackClick = onBackClick,
            onCategoryClick = { categoryId, month ->
                onCategoryClick(RootKey.CategoryDetail(categoryId, month.year, month.monthValue))
            }
        )
    }
}
