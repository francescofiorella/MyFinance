package com.frafio.myfinance.data.enums.db

import com.frafio.myfinance.utils.getCurrentLanguage

object DbPurchases {
    enum class FIELDS(val value: String) {
        PURCHASES("purchases"),
        NAME("name"),
        EMAIL("email"),
        YEAR("year"),
        MONTH("month"),
        DAY("day"),
        TYPE("type"),
        PRICE("price"),
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

    enum class COLLECTIONS(val value: String) {
        ZERO_ONE("2020_2021"),
        ONE_TWO("2021_2022"),
        TWO_THREE("2022_2023"),
        THREE_FOUR("2023_2024")
    }
}