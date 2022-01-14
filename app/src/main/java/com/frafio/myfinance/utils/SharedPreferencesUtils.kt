package com.frafio.myfinance.utils

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.frafio.myfinance.MyFinanceApplication.Companion.COLLECTION_KEY
import com.frafio.myfinance.data.enums.db.DbPurchases

fun getSharedCollection(sharedPreferences: SharedPreferences): String {
    return sharedPreferences.getString(COLLECTION_KEY, DbPurchases.COLLECTIONS.ONE_TWO.value)
        ?: DbPurchases.COLLECTIONS.ONE_TWO.value
}

@SuppressLint("ApplySharedPref")
fun setSharedCollection(sharedPreferences: SharedPreferences, collection: String) {
    sharedPreferences.edit().putString(COLLECTION_KEY, collection).commit()
}