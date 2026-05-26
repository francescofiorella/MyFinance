package com.frafio.myfinance.ui.features.home.budget.navigation

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.data.enums.db.FinanceCode
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.ui.features.home.budget.BudgetScreen
import com.frafio.myfinance.ui.home.budget.BudgetListener
import com.frafio.myfinance.ui.home.budget.BudgetViewModel
import com.frafio.myfinance.ui.navigation.LocalSnackbarHostState
import com.frafio.myfinance.ui.navigation.MyFinanceAppState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import kotlinx.coroutines.launch

fun EntryProviderScope<NavKey>.budgetEntry(
    appState: MyFinanceAppState,
    onEditIncome: (Income, Int) -> Unit,
) {
    entry<MyFinanceNavKey.Budget> {
        val viewModel: BudgetViewModel = hiltViewModel()
        val snackbarHostState = LocalSnackbarHostState.current
        val coroutineScope = rememberCoroutineScope()

        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == MyFinanceNavKey.Budget) {
                    viewModel.scrollToId(null)
                }
            }
        }

        DisposableEffect(viewModel) {
            viewModel.listener = object : BudgetListener {
                override fun onCompleted(
                    response: LiveData<FinanceResult>,
                    previousBudget: Double?
                ) {
                    response.observeForever { result ->
                        when (result.code) {
                            FinanceCode.BUDGET_UPDATE_SUCCESS.code -> {
                                previousBudget?.let {
                                    coroutineScope.launch {
                                        val actionResult = snackbarHostState.showSnackbar(
                                            message = result.message,
                                            actionLabel = undoString
                                        )
                                        if (actionResult == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                            viewModel.setMonthlyBudget(previousBudget)
                                        }
                                    }
                                } ?: run {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                }
                            }
                            else -> {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        }
                    }
                }

                override fun onDeleteCompleted(
                    response: LiveData<FinanceResult>,
                    income: Income
                ) {
                    response.observeForever { result ->
                        if (result.code == FinanceCode.INCOME_DELETE_SUCCESS.code) {
                            coroutineScope.launch {
                                val actionResult = snackbarHostState.showSnackbar(
                                    message = result.message,
                                    actionLabel = undoString
                                )
                                if (actionResult == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                    viewModel.addIncome(income)
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        }
                    }
                }
            }
            onDispose { viewModel.listener = null }
        }

        BudgetScreen(
            viewModel = viewModel,
            onEditIncome = onEditIncome
        )
    }
}
