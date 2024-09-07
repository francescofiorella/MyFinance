package com.frafio.myfinance.data.enums.db

import com.frafio.myfinance.utils.getCurrentLanguage

enum class FinanceCode(val code: Int, val message: String) {
    EMPTY_NAME(
        1, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter the name."
            Languages.ITALIANO.value -> "Inserisci il nome."
            else -> "Enter the name." // english
        }
    ),

    WRONG_NAME_TOTAL(
        2, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The name can't be 'Total'."
            Languages.ITALIANO.value -> "Il nome non può essere 'Totale'."
            else -> "The name can't be 'Total'." // english
        }
    ),

    EMPTY_AMOUNT(
        3, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter the amount."
            Languages.ITALIANO.value -> "Inserisci l'importo."
            else -> "Enter the amount." // english
        }
    ),

    WRONG_AMOUNT(
        4, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The amount can't be zero."
            Languages.ITALIANO.value -> "L'importo non può essere zero."
            else -> "The amount can't be zero." // english
        }
    ),

    EMPTY_CATEGORY(
        5, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter the category."
            Languages.ITALIANO.value -> "Inserisci la categoria."
            else -> "Enter the category." // english
        }
    ),

    EXPENSE_ADD_SUCCESS(
        10, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expense added!"
            Languages.ITALIANO.value -> "Spesa aggiunta!"
            else -> "Expense added!" // english
        }
    ),

    EXPENSE_ADD_FAILURE(
        11, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expense not added!"
            Languages.ITALIANO.value -> "Spesa non aggiunta!"
            else -> "Expense not added!" // english
        }
    ),

    EXPENSE_EDIT_SUCCESS(
        12, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expense edited!"
            Languages.ITALIANO.value -> "Spesa modificata!"
            else -> "Expense edited!" // english
        }
    ),

    EXPENSE_EDIT_FAILURE(
        13, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expense not edited!"
            Languages.ITALIANO.value -> "Spesa non modificata!"
            else -> "Expense not edited!" // english
        }
    ),

    EXPENSE_DELETE_SUCCESS(
        14, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expense deleted!"
            Languages.ITALIANO.value -> "Spesa eliminata!"
            else -> "Expense deleted!" // english
        }
    ),

    EXPENSE_DELETE_FAILURE(
        15, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expense not deleted correctly!"
            Languages.ITALIANO.value -> "Spesa non eliminata correttamente!"
            else -> "Expense not deleted correctly!" // english
        }
    ),

    EXPENSE_LIST_UPDATE_SUCCESS(
        16, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expenses list updated!"
            Languages.ITALIANO.value -> "Lista spese aggiornata!"
            else -> "Expenses list updated!" // english
        }
    ),

    EXPENSE_LIST_UPDATE_FAILURE(
        17, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Expenses list not updated!"
            Languages.ITALIANO.value -> "Lista spese non aggiornata!"
            else -> "Expenses list not updated!" // english
        }
    ),

    INCOME_LIST_UPDATE_SUCCESS(
        20, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Incomes list updated!"
            Languages.ITALIANO.value -> "Lista entrate aggiornata!"
            else -> "Incomes list updated!" // english
        }
    ),

    INCOME_LIST_UPDATE_FAILURE(
        21, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Incomes list not updated!"
            Languages.ITALIANO.value -> "Lista entrate non aggiornata!"
            else -> "Incomes list not updated!" // english
        }
    ),

    INCOME_ADD_SUCCESS(
        22, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income added!"
            Languages.ITALIANO.value -> "Entrata aggiunta!"
            else -> "Income added!" // english
        }
    ),

    INCOME_ADD_FAILURE(
        23, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income not added!"
            Languages.ITALIANO.value -> "Entrata non aggiunta!"
            else -> "Income not added!" // english
        }
    ),

    INCOME_DELETE_SUCCESS(
        24, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income deleted!"
            Languages.ITALIANO.value -> "Entrata eliminata!"
            else -> "Income deleted!" // english
        }
    ),

    INCOME_DELETE_FAILURE(
        25, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income not deleted correctly!"
            Languages.ITALIANO.value -> "Entrata non eliminata correttamente!"
            else -> "Income not deleted correctly!" // english
        }
    ),

    INCOME_EDIT_SUCCESS(
        26, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income edited!"
            Languages.ITALIANO.value -> "Entrata modificata!"
            else -> "Income edited!" // english
        }
    ),

    INCOME_EDIT_FAILURE(
        27, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income not edited!"
            Languages.ITALIANO.value -> "Entrata non modificata!"
            else -> "Income not edited!" // english
        }
    ),

    BUDGET_UPDATE_SUCCESS(
        30, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Budget updated!"
            Languages.ITALIANO.value -> "Budget aggiornato"
            else -> "Budget updated!" // english
        }
    ),

    BUDGET_UPDATE_FAILURE(
        31, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Budget not updated!"
            Languages.ITALIANO.value -> "Budget non aggiornato"
            else -> "Budget not updated!" // english
        }
    )
}