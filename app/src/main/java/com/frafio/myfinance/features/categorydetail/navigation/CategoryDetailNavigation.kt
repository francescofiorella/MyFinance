package com.frafio.myfinance.features.categorydetail.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.categorydetail.CategoryDetailScreen
import com.frafio.myfinance.features.categorydetail.CategoryDetailViewModel

fun EntryProviderScope<NavKey>.categoryDetailEntry(
    onBackClick: () -> Unit,
    onSeeAllExpenses: () -> Unit
) {
    entry<RootKey.CategoryDetail> { key ->
        val viewModel: CategoryDetailViewModel = hiltViewModel(
            creationCallback = { factory: CategoryDetailViewModel.Factory ->
                factory.create(key)
            }
        )
        CategoryDetailScreen(
            viewModel = viewModel,
            onBackClick = onBackClick,
            onSeeAllExpenses = onSeeAllExpenses
        )
    }
}
