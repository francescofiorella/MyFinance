package com.frafio.myfinance.ui.features.home.expenses.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.data.model.Expense
import com.frafio.myfinance.ui.features.home.expenses.ExpensesScreen
import com.frafio.myfinance.ui.home.expenses.ExpensesViewModel
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey
import java.time.LocalDate

fun EntryProviderScope<NavKey>.expensesEntry(
    viewModel: ExpensesViewModel,
    onItemLongClick: (Expense, Int) -> Unit,
    getDateLabel: (LocalDate, LocalDate) -> String,
) {
    entry<MyFinanceNavKey.Expenses> {
        ExpensesScreen(
            viewModel = viewModel,
            onItemLongClick = onItemLongClick,
            getDateLabel = getDateLabel
        )
    }
}
