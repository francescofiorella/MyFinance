package com.frafio.myfinance.features.labeldetail.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.labeldetail.LabelDetailScreen
import com.frafio.myfinance.features.labeldetail.LabelDetailViewModel

fun EntryProviderScope<NavKey>.labelDetailEntry(
    onBackClick: () -> Unit,
    onSeeAllExpenses: () -> Unit,
    onFrequentExpenseClick: (RootKey.FrequentExpenses) -> Unit
) {
    entry<RootKey.LabelDetail> { key ->
        val viewModel: LabelDetailViewModel = hiltViewModel(
            creationCallback = { factory: LabelDetailViewModel.Factory ->
                factory.create(key)
            }
        )
        LabelDetailScreen(
            viewModel = viewModel,
            onBackClick = onBackClick,
            onSeeAllExpenses = onSeeAllExpenses,
            onFrequentExpenseClick = onFrequentExpenseClick
        )
    }
}
