package com.frafio.myfinance.data.enums.db

object DbReceipt {
    enum class FIELDS(val value: String) {
        RECEIPT("receipt"),
        NAME("name"),
        PRICE("price")
    }
}