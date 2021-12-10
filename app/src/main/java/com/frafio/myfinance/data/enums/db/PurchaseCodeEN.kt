package com.frafio.myfinance.data.enums.db

enum class PurchaseCodeEN(val code: Int, val message: String) {
    EMPTY_NAME(1, "Enter the purchase name."),
    EMPTY_PRICE(2, "Enter the purchase price."),
    WRONG_NAME_TOTAL(3, "The purchase name can't be 'Total'"),

    TOTAL_ADD_SUCCESS(10, "Total added!"),
    TOTAL_ADD_FAILURE(11, "Total not added!"),
    PURCHASE_ADD_FAILURE(12, "Purchase not added!"),
    PURCHASE_ADD_ERROR(13, "Purchase not added correctly!"),

    PURCHASE_EDIT_SUCCESS(20, "Purchase edited!"),
    PURCHASE_EDIT_FAILURE(21, "Purchase not edited!"),

    PURCHASE_DELETE_SUCCESS(30, "Purchase deleted!"),
    PURCHASE_DELETE_FAILURE(31, "Purchase not deleted correctly!"),

    PURCHASE_LIST_UPDATE_SUCCESS(40, "List Updated!"),
    PURCHASE_LIST_UPDATE_FAILURE(41, "List not updated!"),

    RECEIPT_ADD_SUCCESS(100, "Item added!"),
    RECEIPT_ADD_FAILURE(102, "Item not added!"),
    RECEIPT_DELETE_SUCCESS(103, "Item deleted!"),
    RECEIPT_DELETE_FAILURE(104, "Item not deleted!")
}