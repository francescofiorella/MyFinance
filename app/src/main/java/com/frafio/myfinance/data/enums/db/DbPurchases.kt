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

    enum class NAMES(val value: String) {
        TOTALE("Totale"),
        AMTAB("Biglietto Amtab"),
        TRENITALIA("Biglietto TrenItalia"),
        TOTALE_ZERO("0.00")
    }

    enum class TYPES(val value: Int) {
        TOTAL(0),
        SHOPPING(1),
        GENERIC(2),
        TICKET(3)
    }

    enum class COLLECTIONS(val value: String) {
        ZERO_UNO("2020_2021"),
        UNO_DUE("2021_2022")
    }
}