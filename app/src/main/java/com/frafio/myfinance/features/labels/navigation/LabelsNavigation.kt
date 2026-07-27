package com.frafio.myfinance.features.labels.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.RootKey
import com.frafio.myfinance.features.labels.LabelsScreen
import com.frafio.myfinance.features.labels.LabelsUiEvent
import com.frafio.myfinance.features.labels.LabelsViewModel
import kotlinx.coroutines.launch

fun EntryProviderScope<NavKey>.labelsEntry(
    appState: MyFinanceAppState,
    onBackClick: () -> Unit
) {
    entry<RootKey.Labels> {
        val viewModel: LabelsViewModel = hiltViewModel()
        val coroutineScope = rememberCoroutineScope()
        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(viewModel.uiEvents) {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is LabelsUiEvent.ShowSnackBar -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                event.message,
                                event.actionText,
                                event.actionFun,
                                event.dismissFun
                            )
                        }
                    }

                    is LabelsUiEvent.LabelDeleted -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                event.message,
                                undoString,
                                {
                                    viewModel.undoDeleteLabel(appState.coroutineScope)
                                },
                                viewModel::resetLastDeletedLabel
                            )
                        }
                    }
                }
            }
        }

        LabelsScreen(
            appState = appState,
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}
