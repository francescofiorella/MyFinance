package com.frafio.myfinance.data.enums.db

import com.frafio.myfinance.utils.getCurrentLanguage

enum class PurchaseCode(val code: Int, val message: String) {
    EMPTY_NAME(
        1, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter the purchase name."
            Languages.ITALIANO.value -> "Inserisci il nome dell'acquisto."
            else -> "Enter the purchase name." // english
        }
    ),

    EMPTY_PRICE(
        2, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter the purchase price."
            Languages.ITALIANO.value -> "Inserisci il costo dell'acquisto."
            else -> "Enter the purchase price." // english
        }
    ),

    WRONG_NAME_TOTAL(
        3, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "The purchase name can't be 'Total'."
            Languages.ITALIANO.value -> "L'acquisto non può chiamarsi 'Totale'."
            else -> "The purchase name can't be 'Total'." // english
        }
    ),

    EMPTY_CATEGORY(
        4, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Enter the purchase category."
            Languages.ITALIANO.value -> "Inserisci la categoria dell'acquisto."
            else -> "Enter the purchase category." // english
        }
    ),

    PURCHASE_ADD_SUCCESS(
        10, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase added!"
            Languages.ITALIANO.value -> "Acquisto aggiunto!"
            else -> "Purchase added!" // english
        }
    ),

    PURCHASE_ADD_FAILURE(
        11, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase not added!"
            Languages.ITALIANO.value -> "Acquisto non aggiunto!"
            else -> "Purchase not added!" // english
        }
    ),

    PURCHASE_EDIT_SUCCESS(
        12, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase edited!"
            Languages.ITALIANO.value -> "Acquisto modificato!"
            else -> "Purchase edited!" // english
        }
    ),

    PURCHASE_EDIT_FAILURE(
        13, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase not edited!"
            Languages.ITALIANO.value -> "Acquisto non modificato!"
            else -> "Purchase not edited!" // english
        }
    ),

    PURCHASE_DELETE_SUCCESS(
        14, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase deleted!"
            Languages.ITALIANO.value -> "Acquisto eliminato!"
            else -> "Purchase deleted!" // english
        }
    ),

    PURCHASE_DELETE_FAILURE(
        15, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase not deleted correctly!"
            Languages.ITALIANO.value -> "Acquisto non eliminato correttamente!"
            else -> "Purchase not deleted correctly!" // english
        }
    ),

    PURCHASE_LIST_UPDATE_SUCCESS(
        16, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "List updated!"
            Languages.ITALIANO.value -> "Lista aggiornata!"
            else -> "List Updated!" // english
        }
    ),

    PURCHASE_LIST_UPDATE_FAILURE(
        17, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "List not updated!"
            Languages.ITALIANO.value -> "Lista non aggiornata!"
            else -> "List not updated!" // english
        }
    ),

    INCOME_LIST_UPDATE_SUCCESS(
        20, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income list updated!"
            Languages.ITALIANO.value -> "Lista delle entrate aggiornata!"
            else -> "Income list updated!" // english
        }
    ),

    INCOME_LIST_UPDATE_FAILURE(
        21, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Income list not updated!"
            Languages.ITALIANO.value -> "Lista delle entrate non aggiornata!"
            else -> "Income list not updated!" // english
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