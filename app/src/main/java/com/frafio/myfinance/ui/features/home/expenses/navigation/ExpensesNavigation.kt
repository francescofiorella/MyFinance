package com.frafio.myfinance.ui.features.home.expenses.navigation

import androidx.compose.material3.SnackbarResult
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
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.data.model.FinanceResult
import com.frafio.myfinance.ui.features.home.expenses.ExpensesScreen
import com.frafio.myfinance.ui.home.expenses.ExpensesListener
import com.frafio.myfinance.ui.home.expenses.ExpensesViewModel
import com.frafio.myfinance.ui.navigation.LocalSnackbarHostState
import com.frafio.myfinance.ui.navigation.MyFinanceAppState
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import kotlinx.coroutines.launch
import java.time.LocalDate

fun EntryProviderScope<NavKey>.expensesEntry(
    appState: MyFinanceAppState,
    onItemLongClick: (Expense, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
) {
    entry<MyFinanceNavKey.Expenses> {
        val viewModel: ExpensesViewModel = hiltViewModel()
        val snackbarHostState = LocalSnackbarHostState.current
        val coroutineScope = rememberCoroutineScope()

        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == MyFinanceNavKey.Expenses) {
                    val today = LocalDate.now()
                    val todayId = "total_${today.dayOfMonth}_${today.monthValue}_${today.year}"
                    viewModel.scrollToId(todayId)
                }
            }
        }

        DisposableEffect(viewModel) {
            viewModel.listener = object : ExpensesListener {
                override fun onCompleted(response: LiveData<FinanceResult>) {
                    response.observeForever { result ->
                        if (result.code == FinanceCode.EXPENSE_ADD_SUCCESS.code) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        }
                    }
                }

                override fun onDeleteCompleted(
                    response: LiveData<FinanceResult>,
                    expense: Expense
                ) {
                    response.observeForever { result ->
                        if (result.code == FinanceCode.EXPENSE_DELETE_SUCCESS.code) {
                            coroutineScope.launch {
                                val actionResult = snackbarHostState.showSnackbar(
                                    message = result.message,
                                    actionLabel = undoString
                                )
                                if (actionResult == SnackbarResult.ActionPerformed) {
                                    viewModel.addExpense(expense)
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(result.message)
                            }
                        }
                    }
                }

                override fun onDeleteCompleted(
                    response: LiveData<FinanceResult>
                ) {
                    response.observeForever { result ->
                        if (result.code == FinanceCode.LABEL_DELETE_SUCCESS.code) {
                            coroutineScope.launch {
                                val actionResult = snackbarHostState.showSnackbar(
                                    message = result.message,
                                    actionLabel = undoString
                                )
                                if (actionResult == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDeleteLabel()
                                } else {
                                    viewModel.resetLastDeletedLabel()
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

        ExpensesScreen(
            viewModel = viewModel,
            onItemLongClick = onItemLongClick,
            getDateLabel = getDateLabel
        )
    }
}
