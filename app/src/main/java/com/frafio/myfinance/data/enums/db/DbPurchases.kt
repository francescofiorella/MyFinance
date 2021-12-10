package com.frafio.myfinance.data.enums.db

object DbPurchases {
    enum class FIELDS(val value: String) {
        PURCHASES("purchases"),
        NAME("name"),
        EMAIL("email"),
        YEAR("year"),
        MONTH("month"),
        DAY("day"),
        TYPE("type"),
        PRICE("price")
    }

    enum class NAMES(val value_en: String, val value_it: String) {
        RENT("Rent","Affitto"),
        TOTAL("Total","Totale"),
        TOTAL_PRICE("0.00", "0.00")
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
        ONE_TWO("2021_2022")
    }
}