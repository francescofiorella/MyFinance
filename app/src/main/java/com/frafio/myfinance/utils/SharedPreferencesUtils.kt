package com.frafio.myfinance.utils

import android.content.SharedPreferences
import com.frafio.myfinance.MyFinanceApplication.Companion.COLLECTION_KEY
import com.frafio.myfinance.data.enums.db.DbPurchases

fun getSharedCollection(sharedPreferences: SharedPreferences): String {
    return sharedPreferences.getString(COLLECTION_KEY, DbPurchases.COLLECTIONS.UNO_DUE.value)
        ?: DbPurchases.COLLECTIONS.UNO_DUE.value
}

fun setSharedCollection(sharedPreferences: SharedPreferences, collection: String) {
    sharedPreferences.edit().putString(COLLECTION_KEY, collection).commit()
}