package com.frafio.myfinance.features.home.expenses.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.R
import com.frafio.myfinance.core.data.model.Expense
import com.frafio.myfinance.features.home.expenses.ExpensesScreen
import com.frafio.myfinance.features.home.expenses.ExpensesUiEvent
import com.frafio.myfinance.features.home.expenses.ExpensesViewModel
import com.frafio.myfinance.core.navigation.MyFinanceAppState
import com.frafio.myfinance.core.navigation.HomeTabKey
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

fun EntryProviderScope<NavKey>.expensesEntry(
    appState: MyFinanceAppState,
    parentScrollEvents: Flow<Pair<String, Boolean>?>,
    resetParentScrollEvents: () -> Unit,
    onItemLongClick: (Expense, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
) {
    entry<HomeTabKey.Expenses> {
        val viewModel: ExpensesViewModel = hiltViewModel()

        val expenseDeletedString = stringResource(id = R.string.expense_deleted)
        val labelDeletedString = stringResource(id = R.string.label_deleted)
        val undoString = stringResource(id = R.string.undo)

        LaunchedEffect(appState.reselectEvent) {
            appState.reselectEvent.collect { key ->
                if (key == HomeTabKey.Expenses) {
                    val today = LocalDate.now()
                    val todayId = "total_${today.dayOfMonth}_${today.monthValue}_${today.year}"
                    viewModel.scrollToId(todayId)
                }
            }
        }

        LaunchedEffect(parentScrollEvents) {
            parentScrollEvents.collect { event ->
                if (event != null) {
                    val (id, isExpense) = event
                    if (isExpense) {
                        viewModel.scrollToId(id)
                        resetParentScrollEvents()
                    }
                }
            }
        }

        LaunchedEffect(viewModel.uiEvents) {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is ExpensesUiEvent.ShowSnackBar -> {
                        appState.showSnackBar(
                            event.message,
                            event.actionText,
                            event.actionFun,
                            event.dismissFun
                        )
                    }

                    is ExpensesUiEvent.ExpenseDeleted -> {
                        appState.showSnackBar(
                            expenseDeletedString,
                            undoString,
                            {
                                viewModel.addExpense(event.expense, notify = false)
                            }
                        )
                    }

                    ExpensesUiEvent.LabelDeleted -> {
                        appState.showSnackBar(
                            labelDeletedString,
                            undoString,
                            viewModel::undoDeleteLabel,
                            viewModel::resetLastDeletedLabel
                        )
                    }
                }
            }
        }

        ExpensesScreen(
            viewModel = viewModel,
            onItemLongClick = onItemLongClick,
            getDateLabel = getDateLabel
        )
    }
}
