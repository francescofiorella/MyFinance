package com.frafio.myfinance.features.managelabels.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.managelabels.ManageLabelsScreen
import com.frafio.myfinance.features.managelabels.ManageLabelsUiEvent
import com.frafio.myfinance.features.managelabels.ManageLabelsViewModel
import kotlinx.coroutines.launch

fun EntryProviderScope<NavKey>.manageLabelsEntry(
    appState: MyFinanceAppState,
    onBackClick: () -> Unit
) {
    entry<RootKey.ManageLabels> {
        val viewModel: ManageLabelsViewModel = hiltViewModel()
        val coroutineScope = rememberCoroutineScope()
        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(viewModel.uiEvents) {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is ManageLabelsUiEvent.ShowSnackBar -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                event.message,
                                event.actionText,
                                event.actionFun,
                                event.dismissFun
                            )
                        }
                    }

                    is ManageLabelsUiEvent.LabelDeleted -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                event.message,
                                undoString,
                                {
                                    viewModel.undoDeleteLabel(
                                        appState.coroutineScope,
                                        event.label,
                                        event.affectedExpenses
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        ManageLabelsScreen(
            appState = appState,
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}
