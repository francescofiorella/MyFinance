package com.frafio.myfinance.core.data.enums.db

import com.frafio.myfinance.core.utils.getCurrentLanguage

object FirestoreEnums {
    enum class FIELDS(val value: String) {
        PURCHASES("purchases"),
        MONTHLY_BUDGET("monthly_budget"),
        CURRENCY_CODE("currencyCode"),
        PAYMENTS("payments"),
        INCOMES("incomes"),
        NAME("name"),
        YEAR("year"),
        MONTH("month"),
        DAY("day"),
        TIMESTAMP("timestamp"),
        PRICE("price"),
        CATEGORY("category"),
        LABELS("labels"),
        ID("id"),
        UPDATED_AT("updatedAt"),
        IS_DELETED("isDeleted"),
        DELETE_AT("deleteAt"),
        PRO_PIC_CHOICE("proPicChoice")
    }

    enum class PRO_PIC_TYPES(val value: String) {
        AVATAR_1("avatar_1"),
        AVATAR_2("avatar_2"),
        AVATAR_3("avatar_3"),
        AVATAR_4("avatar_4"),
        AVATAR_5("avatar_5"),
        GOOGLE("google")
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
        HOUSING(0),
        GROCERIES(1),
        PERSONAL_CARE(2),
        ENTERTAINMENT(3),
        EDUCATION(4),
        DINING(5),
        HEALTH(6),
        TRANSPORTATION(7),
        MISCELLANEOUS(8),
        TOTAL(100),
        INCOME(101),
        JOLLY(102)
    }
}