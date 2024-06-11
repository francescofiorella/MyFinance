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
        20, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase edited!"
            Languages.ITALIANO.value -> "Acquisto modificato!"
            else -> "Purchase edited!" // english
        }
    ),

    PURCHASE_EDIT_FAILURE(
        21, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase not edited!"
            Languages.ITALIANO.value -> "Acquisto non modificato!"
            else -> "Purchase not edited!" // english
        }
    ),

    PURCHASE_DELETE_SUCCESS(
        30, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase deleted!"
            Languages.ITALIANO.value -> "Acquisto eliminato!"
            else -> "Purchase deleted!" // english
        }
    ),

    PURCHASE_DELETE_FAILURE(
        31, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Purchase not deleted correctly!"
            Languages.ITALIANO.value -> "Acquisto non eliminato correttamente!"
            else -> "Purchase not deleted correctly!" // english
        }
    ),

    PURCHASE_LIST_UPDATE_SUCCESS(
        40, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "List Updated!"
            Languages.ITALIANO.value -> "Lista aggiornata!"
            else -> "List Updated!" // english
        }
    ),

    PURCHASE_LIST_UPDATE_FAILURE(
        41, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "List not updated!"
            Languages.ITALIANO.value -> "Lista non aggiornata!"
            else -> "List not updated!" // english
        }
    ),

    PURCHASE_GET_CATEGORIES_SUCCESS(
        50, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Categories list received!"
            Languages.ITALIANO.value -> "Lista categorie ricevuta!"
            else -> "Categories list received!" // english
        }
    ),

    PURCHASE_GET_CATEGORIES_FAILURE(
        51, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Categories list not received!"
            Languages.ITALIANO.value -> "Lista categorie non ricevuta!"
            else -> "Categories list not received!" // english
        }
    ),

    PURCHASE_CREATE_CATEGORY_SUCCESS(
        52, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Category created!"
            Languages.ITALIANO.value -> "Categoria creata!"
            else -> "Category created!" // english
        }
    ),

    PURCHASE_CREATE_CATEGORY_FAILURE(
        52, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Category not created!"
            Languages.ITALIANO.value -> "Categoria non creata!"
            else -> "Category not created!" // english
        }
    ),

    INVOICE_ADD_SUCCESS(
        100, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Item added!"
            Languages.ITALIANO.value -> "Voce aggiunta!"
            else -> "Item added!" // english
        }
    ),

    INVOICE_ADD_FAILURE(
        102, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Item not added!"
            Languages.ITALIANO.value -> "Voce non aggiunta!"
            else -> "Item not added!" // english
        }
    ),

    INVOICE_DELETE_SUCCESS(
        103, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Item deleted!"
            Languages.ITALIANO.value -> "Voce eliminata!"
            else -> "Item deleted!" // english
        }
    ),

    INVOICE_DELETE_FAILURE(
        104, when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "Item not deleted!"
            Languages.ITALIANO.value -> "Voce non eliminata!"
            else -> "Item not deleted!" // english
        }
    )
}