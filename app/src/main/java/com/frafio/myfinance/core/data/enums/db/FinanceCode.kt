package com.frafio.myfinance.core.data.enums.db

import androidx.annotation.StringRes
import com.frafio.myfinance.R

enum class FinanceCode(val code: Int, @StringRes val messageRes: Int) {
    EMPTY_NAME(1, R.string.error_empty_name),
    WRONG_NAME_TOTAL(2, R.string.error_name_total),
    EMPTY_AMOUNT(3, R.string.error_empty_amount),
    WRONG_AMOUNT(4, R.string.error_zero_amount),
    EMPTY_CATEGORY(5, R.string.error_empty_category),
    EXPENSE_ADD_SUCCESS(10, R.string.expense_added),
    EXPENSE_ADD_FAILURE(11, R.string.expense_not_added),
    EXPENSE_EDIT_SUCCESS(12, R.string.expense_updated),
    EXPENSE_EDIT_FAILURE(13, R.string.expense_not_updated),
    EXPENSE_DELETE_SUCCESS(14, R.string.expense_deleted),
    EXPENSE_DELETE_FAILURE(15, R.string.expense_not_deleted),
    EXPENSE_LIST_UPDATE_SUCCESS(16, R.string.expenses_list_updated),
    EXPENSE_LIST_UPDATE_FAILURE(17, R.string.expenses_list_not_updated),
    INCOME_LIST_UPDATE_SUCCESS(20, R.string.incomes_list_updated),
    INCOME_LIST_UPDATE_FAILURE(21, R.string.incomes_list_not_updated),
    INCOME_ADD_SUCCESS(22, R.string.income_added),
    INCOME_ADD_FAILURE(23, R.string.income_not_added),
    INCOME_DELETE_SUCCESS(24, R.string.income_deleted),
    INCOME_DELETE_FAILURE(25, R.string.income_not_deleted),
    INCOME_EDIT_SUCCESS(26, R.string.income_updated),
    INCOME_EDIT_FAILURE(27, R.string.income_not_updated),
    BUDGET_UPDATE_SUCCESS(30, R.string.budget_updated),
    BUDGET_UPDATE_FAILURE(31, R.string.budget_not_updated),
    LABELS_UPDATE_SUCCESS(40, R.string.labels_updated),
    LABELS_UPDATE_FAILURE(41, R.string.labels_not_updated),
    LABEL_DELETE_SUCCESS(42, R.string.label_deleted),
    LABEL_UPDATE_SUCCESS(43, R.string.label_updated),
    LABEL_ADD_SUCCESS(44, R.string.label_created)
}
