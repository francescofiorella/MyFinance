package com.frafio.myfinance.features.add.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.features.add.AddUiEvent
import com.frafio.myfinance.features.add.AddViewModel
import com.frafio.myfinance.features.add.AddScreen
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey

fun EntryProviderScope<NavKey>.addEntry(
    appState: MyFinanceAppState,
    onBackClick: () -> Unit,
    onSaveSuccess: (Boolean, Int, Int, Int) -> Unit
) {
    entry<RootKey.AddEditTransaction> { key ->
        val viewModel: AddViewModel = hiltViewModel(
            creationCallback = { factory: AddViewModel.Factory ->
                factory.create(key)
            }
        )

        LaunchedEffect(viewModel.uiEvents) {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is AddUiEvent.Success -> {
                        appState.showSnackBar(event.result.message)
                        onSaveSuccess(event.isExpense, event.day, event.month, event.year)
                    }
                    is AddUiEvent.Error -> {
                        viewModel.updateAddingState(true)
                        appState.showSnackBar(event.result.message)
                    }
                }
            }
        }

        AddScreen(
            appState = appState,
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}
