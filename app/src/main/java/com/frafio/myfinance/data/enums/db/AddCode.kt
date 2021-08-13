package com.frafio.myfinance.data.enums.db

enum class AddCode(val code: Int, val message: String) {
    RECEIPT_ADD_SUCCESS(1, "Voce aggiunta!"),
    RECEIPT_ADD_FAILURE(2, "Voce non aggiunta!"),
    RECEIPT_DELETE_SUCCESS(3, "Voce eliminata!"),
    RECEIPT_DELETE_FAILURE(4, "Voce non eliminata!"),

    EMPTY_NAME(10, "Inserisci il nome dell'acquisto."),
    EMPTY_PRICE(11, "Inserisci il costo dell'acquisto.")
}