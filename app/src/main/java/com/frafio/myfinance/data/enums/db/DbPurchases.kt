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
    }

    enum class NAMES(val value: String, val value_en: String, val value_it: String) {
        TOTAL(
            when (getCurrentLanguage()) {
                Languages.ENGLISH.value -> "Total"
                Languages.ITALIANO.value -> "Totale"
                else -> "Total" // english
            }, "Total", "Totale"
        )
    }

    enum class TYPES(val value: Int) {
        TOTAL(100),
        HOUSING(0),
        GROCERIES(1),
        PERSONAL_CARE(2),
        ENTERTAINMENT(3),
        EDUCATION(4),
        DINING(5),
        HEALTH(6),
        TRANSPORTATION(7),
        MISCELLANEOUS(8)
    }
}