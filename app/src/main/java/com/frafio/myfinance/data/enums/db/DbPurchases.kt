package com.frafio.myfinance.data.enums.db

import com.frafio.myfinance.utils.getCurrentLanguage

object DbPurchases {
    enum class FIELDS(val value: String) {
        PURCHASES("purchases"),
        PAYMENTS("payments"),
        NAME("name"),
        EMAIL("email"),
        YEAR("year"),
        MONTH("month"),
        DAY("day"),
        TYPE("type"),
        PRICE("price"),
        CATEGORY("category"),
        CATEGORIES("categories")
    }

    enum class NAMES(val value: String, val value_en: String, val value_it: String) {
        RENT(
            when (getCurrentLanguage()) {
                Languages.ENGLISH.value -> "Rent"
                Languages.ITALIANO.value -> "Affitto"
                else -> "Rent" // english
            }, "Rent", "Affitto"
        ),
        TOTAL(
            when (getCurrentLanguage()) {
                Languages.ENGLISH.value -> "Total"
                Languages.ITALIANO.value -> "Totale"
                else -> "Total" // english
            }, "Total", "Totale"
        ),
        TOTAL_PRICE("0.00", "0.00", "0.00")
    }

    enum class TYPES(val value: Int) {
        TOTAL(0),
        SHOPPING(1),
        GENERIC(2),
        TRANSPORT(3),
        RENT(4)
    }

    enum class CATEGORIES(val value: String) {
        DEFAULT(when (getCurrentLanguage()) {
            Languages.ENGLISH.value -> "First category"
            Languages.ITALIANO.value -> "Prima categoria"
            else -> "First category" // english
        })
    }
}