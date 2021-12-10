package com.frafio.myfinance.data.enums.db

enum class PurchaseCodeIT(val code: Int, val message: String) {
    EMPTY_NAME(1, "Inserisci il nome dell'acquisto."),
    EMPTY_PRICE(2, "Inserisci il costo dell'acquisto."),
    WRONG_NAME_TOTAL(3, "L'acquisto non può chiamarsi 'Totale'."),

    TOTAL_ADD_SUCCESS(10, "Totale aggiunto!"),
    TOTAL_ADD_FAILURE(11, "Totale non aggiunto!"),
    PURCHASE_ADD_FAILURE(12, "Acquisto non aggiunto!"),
    PURCHASE_ADD_ERROR(13, "Acquisto non aggiunto correttamente!"),

    PURCHASE_EDIT_SUCCESS(20, "Acquisto modificato!"),
    PURCHASE_EDIT_FAILURE(21, "Acquisto non modificato!"),

    PURCHASE_DELETE_SUCCESS(30, "Acquisto eliminato!"),
    PURCHASE_DELETE_FAILURE(31, "Acquisto non eliminato correttamente!"),

    PURCHASE_LIST_UPDATE_SUCCESS(40, "Lista aggiornata!"),
    PURCHASE_LIST_UPDATE_FAILURE(41, "Lista non aggiornata!"),

    RECEIPT_ADD_SUCCESS(100, "Voce aggiunta!"),
    RECEIPT_ADD_FAILURE(102, "Voce non aggiunta!"),
    RECEIPT_DELETE_SUCCESS(103, "Voce eliminata!"),
    RECEIPT_DELETE_FAILURE(104, "Voce non eliminata!")
}