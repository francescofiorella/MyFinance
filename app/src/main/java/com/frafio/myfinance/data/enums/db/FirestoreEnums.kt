package com.frafio.myfinance.data.enums.db

import com.frafio.myfinance.utils.getCurrentLanguage

object FirestoreEnums {
    enum class FIELDS(val value: String) {
        PURCHASES("purchases"),
        MONTHLY_BUDGET("monthly_budget"),
        PAYMENTS("payments"),
        INCOMES("incomes"),
        NAME("name"),
        YEAR("year"),
        MONTH("month"),
        DAY("day"),
        TIMESTAMP("timestamp"),
        PRICE("price"),
        CATEGORY("category"),
        ID("id")
    }

    enum class NAMES(val value: String, val valueEn: String, val valueIt: String) {
        TOTAL(
            when (getCurrentLanguage()) {
                Languages.ENGLISH.value -> "Total"
                Languages.ITALIANO.value -> "Totale"
                else -> "Total" // english
            }, "Total", "Totale"
        )
    }

    enum class CATEGORIES(val value: Int) {
        TOTAL(100),
        INCOME(101),
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