package com.frafio.myfinance.ui.features.home.budget.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.frafio.myfinance.data.model.Income
import com.frafio.myfinance.ui.features.home.budget.BudgetScreen
import com.frafio.myfinance.ui.home.budget.BudgetViewModel
import com.frafio.myfinance.ui.navigation.MyFinanceNavKey

fun EntryProviderScope<NavKey>.budgetEntry(
    viewModel: BudgetViewModel,
    onEditIncome: (Income, Int) -> Unit,
) {
    entry<MyFinanceNavKey.Budget> {
        BudgetScreen(
            viewModel = viewModel,
            onEditIncome = onEditIncome
        )
    }
}
