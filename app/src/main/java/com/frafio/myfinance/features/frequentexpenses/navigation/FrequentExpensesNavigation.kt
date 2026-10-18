package com.frafio.myfinance.features.frequentexpenses.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.frequentexpenses.FrequentExpensesScreen
import com.frafio.myfinance.features.frequentexpenses.FrequentExpensesViewModel

fun EntryProviderScope<NavKey>.frequentExpensesEntry(
    onBackClick: () -> Unit
) {
    entry<RootKey.FrequentExpenses> { key ->
        val viewModel: FrequentExpensesViewModel = hiltViewModel(
            creationCallback = { factory: FrequentExpensesViewModel.Factory ->
                factory.create(key)
            }
        )
        FrequentExpensesScreen(viewModel = viewModel, onBackClick = onBackClick)
    }
}
