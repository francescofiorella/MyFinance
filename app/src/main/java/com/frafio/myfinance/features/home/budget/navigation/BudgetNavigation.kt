package com.frafio.myfinance.features.home.budget.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.Income
import com.frafio.myfinance.features.home.budget.BudgetScreen
import com.frafio.myfinance.features.home.budget.BudgetUiEvent
import com.frafio.myfinance.features.home.budget.BudgetViewModel
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.HomeTabKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun EntryProviderScope<NavKey>.budgetEntry(
    appState: MyFinanceAppState,
    parentScrollEvents: Flow<Pair<String, Boolean>?>,
    resetParentScrollEvents: () -> Unit,
    onEditIncome: (Income, Int) -> Unit,
) {
    entry<HomeTabKey.Budget> {
        val viewModel: BudgetViewModel = hiltViewModel()
        val coroutineScope = rememberCoroutineScope()

        val budgetUpdatedString = stringResource(id = R.string.budget_updated)
        val incomeDeletedString = stringResource(id = R.string.income_deleted)
        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == HomeTabKey.Budget) {
                    viewModel.scrollToId(null)
                }
            }
        }

        LaunchedEffect(parentScrollEvents) {
            parentScrollEvents.collect { event ->
                if (event != null) {
                    val (id, isExpense) = event
                    if (!isExpense) {
                        viewModel.scrollToId(id)
                        resetParentScrollEvents()
                    }
                }
            }
        }

        LaunchedEffect(viewModel.uiEvents) {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is BudgetUiEvent.ShowSnackBar -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                event.message,
                                event.actionText,
                                event.actionFun,
                                event.dismissFun
                            )
                        }
                    }

                    is BudgetUiEvent.BudgetUpdated -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                budgetUpdatedString,
                                undoString,
                                {
                                    viewModel.setMonthlyBudget(
                                        event.previousBudget,
                                        notify = false
                                    )
                                }
                            )
                        }
                    }

                    is BudgetUiEvent.IncomeDeleted -> {
                        coroutineScope.launch {
                            appState.showSnackBar(
                                incomeDeletedString,
                                undoString,
                                {
                                    viewModel.addIncome(event.income, notify = false)
                                }
                            )
                        }
                    }
                }
            }
        }

        BudgetScreen(
            viewModel = viewModel,
            onEditIncome = onEditIncome
        )
    }
}
